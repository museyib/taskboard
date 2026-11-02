package llc.devlab.app.security.dto;

public record LoginResponse(
    String email,
    String accessToken,
    long expiresIn,
    String refreshToken
) {}
