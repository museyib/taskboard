package llc.devlab.app.security.dto;

public record RefreshTokenResponse(
    String accessToken,
    long expiresIn,
    String refreshToken
) {}
