package llc.devlab.app.security.service;

import jakarta.transaction.Transactional;
import llc.devlab.app.security.dto.*;
import llc.devlab.app.security.entity.AppUser;
import llc.devlab.app.exception.UserLockException;
import llc.devlab.app.security.mapper.UserMapper;
import llc.devlab.app.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static llc.devlab.app.security.service.LoginFailService.MAX_LOGIN_FAILS;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginFailService loginFailService;
    private final UserMapper userMapper;

    @Transactional
    public UserDto create(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");

        AppUser user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));

        AppUser saved = userRepository.save(user);

        return userMapper.toDto(saved);
    }

    public LoginResponse authenticate(LoginRequest request) {
        AppUser user = userRepository.findByEmail(request.email()).orElse(null);

        loginFailService.checkLockStatus(request.email());

        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            int attempts = 0;
            if (user != null)
                attempts = loginFailService.saveLoginFail(request.email());

            if (attempts == MAX_LOGIN_FAILS - 1)
                throw new UserLockException("You have one more attempt before your account is locked");

            loginFailService.checkLockStatus(request.email());

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        loginFailService.unlockUser(request.email());

        TokenInfo accessToken = tokenService.generateAccessToken(user.getEmail());
        TokenInfo refreshToken = tokenService.generateRefreshToken(user.getEmail());

        return new LoginResponse(
                user.getEmail(),
                accessToken.token(),
                accessToken.expiresIn(),
                refreshToken.token());
    }

    public RefreshTokenResponse refresh(TokenRequest request) {
        return tokenService.refresh(request);
    }

    public void deleteRefreshToken(TokenRequest request) {
        tokenService.deleteRefreshToken(request.token());
    }
}
