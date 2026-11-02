package llc.devlab.app.security.service;

import jakarta.transaction.Transactional;
import llc.devlab.app.security.dto.RefreshTokenResponse;
import llc.devlab.app.security.dto.TokenInfo;
import llc.devlab.app.security.dto.TokenRequest;
import llc.devlab.app.security.entity.RefreshToken;
import llc.devlab.app.security.repository.RefreshTokenRepository;
import llc.devlab.app.security.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenInfo generateAccessToken(String username) {
        return jwtUtil.generateAccessToken(username);
    }

    public TokenInfo generateAccessTokenFromRefreshToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);
        return jwtUtil.generateAccessToken(username);
    }

    @Transactional
    public TokenInfo generateRefreshToken(String username) {
        TokenInfo tokenInfo = jwtUtil.generateRefreshToken(username);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenInfo.token());
        refreshToken.setExpirationDate(Instant.ofEpochMilli(tokenInfo.expiresIn()));
        refreshTokenRepository.save(refreshToken);
        return new TokenInfo(tokenInfo.token(), tokenInfo.expiresIn());
    }

    @Transactional
    public void deleteRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token);
        if (refreshToken == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");

        refreshTokenRepository.delete(refreshToken);
        refreshTokenRepository.flush();
    }

    @Transactional
    public RefreshTokenResponse refresh(TokenRequest request) {
        RefreshToken current = refreshTokenRepository.findByToken(request.token());
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        if (current.getExpirationDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(current);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired. Please login again");
        }

        String username = jwtUtil.extractUsername(request.token());

        refreshTokenRepository.delete(current);
        refreshTokenRepository.flush();

        TokenInfo access = jwtUtil.generateAccessToken(username);
        TokenInfo refresh = generateRefreshToken(username);

        return new RefreshTokenResponse(
                access.token(),
                access.expiresIn(),
                refresh.token()
        );
    }
}
