package llc.devlab.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import llc.devlab.app.security.dto.LoginRequest;
import llc.devlab.app.security.dto.RegisterRequest;
import llc.devlab.app.security.dto.TokenRequest;
import llc.devlab.app.security.repository.LoginFailRepository;
import llc.devlab.app.security.repository.RefreshTokenRepository;
import llc.devlab.app.security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SecurityIntegrationTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private LoginFailRepository loginFailRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        refreshTokenRepository.deleteAll();
        loginFailRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testUserRegistrationAndLogin() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("user@example.com", "Password123!", "John Doe", "USER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("user@example.com")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        // Duplicate registration
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());

        // Login
        LoginRequest loginRequest = new LoginRequest("user@example.com", "Password123!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.email", is("user@example.com")));
    }

    @Test
    void testRefreshTokenRotationAndReplayProtection() throws Exception {
        // Register & Login
        RegisterRequest registerRequest = new RegisterRequest("user@example.com", "Password123!", "John Doe", "USER");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("user@example.com", "Password123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseStr = loginResult.getResponse().getContentAsString();
        String initialRefreshToken = objectMapper.readTree(responseStr).get("refreshToken").asText();

        // Refresh token
        TokenRequest refreshReq = new TokenRequest(initialRefreshToken);
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andReturn();

        String refreshedResponseStr = refreshResult.getResponse().getContentAsString();
        String newRefreshToken = objectMapper.readTree(refreshedResponseStr).get("refreshToken").asText();
        assertFalse(initialRefreshToken.equals(newRefreshToken));

        // Replay attack with initialRefreshToken should fail
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isUnauthorized());

        // Logout with newRefreshToken
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenRequest(newRefreshToken))))
                .andExpect(status().isOk());

        // Refresh with logged-out token should fail
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenRequest(newRefreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testBruteForceLockoutProtection() throws Exception {
        // Register
        RegisterRequest registerRequest = new RegisterRequest("lock@example.com", "Password123!", "Lock User", "USER");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest wrongLogin = new LoginRequest("lock@example.com", "WrongPassword");

        // Attempts 1, 2, 3 -> 401 Unauthorized
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongLogin)))
                    .andExpect(status().isUnauthorized());
        }

        // Attempt 4 -> warning (403 Forbidden with UserLockException)
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLogin)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("one more attempt before your account is locked")));

        // Attempt 5 -> account locked (403 Forbidden)
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLogin)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("User is locked until")));

        // Subsequent attempt with correct password while locked -> 403 Forbidden
        LoginRequest correctLogin = new LoginRequest("lock@example.com", "Password123!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctLogin)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("User is locked until")));
    }

    @Test
    void testRoleBasedAccessControlAndProfile() throws Exception {
        // Register USER
        RegisterRequest userReg = new RegisterRequest("regular@example.com", "Password123!", "Regular User", "USER");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReg)))
                .andExpect(status().isCreated());

        // Register ADMIN
        RegisterRequest adminReg = new RegisterRequest("admin@example.com", "Password123!", "Admin User", "ADMIN");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminReg)))
                .andExpect(status().isCreated());

        // Login USER
        MvcResult userLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("regular@example.com", "Password123!"))))
                .andExpect(status().isOk())
                .andReturn();
        String userToken = objectMapper.readTree(userLogin.getResponse().getContentAsString()).get("accessToken").asText();

        // Login ADMIN
        MvcResult adminLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@example.com", "Password123!"))))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = objectMapper.readTree(adminLogin.getResponse().getContentAsString()).get("accessToken").asText();

        // Unauthenticated access
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }
}
