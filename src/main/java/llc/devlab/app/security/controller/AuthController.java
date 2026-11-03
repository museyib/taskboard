package llc.devlab.app.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import llc.devlab.app.security.dto.*;
import llc.devlab.app.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            description = "Register a new user",
            summary = "Register a new user",
            tags = {"Authentication"}
    )
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        UserDto userDto = authService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @Operation(
            description = "Authenticate a user",
            summary = "Authenticate a user",
            tags = {"Authentication"}
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            description = "Refresh a user's access token",
            summary = "Refresh a user's access token",
            tags = {"Authentication"}
    )
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody TokenRequest request) {
        RefreshTokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            description = "Logout a user",
            summary = "Logout a user",
            tags = {"Authentication"}
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody TokenRequest request) {
        authService.deleteRefreshToken(request);
        return ResponseEntity.ok().build();
    }
}
