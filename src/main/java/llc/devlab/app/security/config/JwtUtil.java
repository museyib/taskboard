package llc.devlab.app.security.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import llc.devlab.app.security.dto.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {
    private final TokenProperties tokenProperties;

    public TokenInfo generateAccessToken(String username) {
        long expiration = System.currentTimeMillis() + tokenProperties.getAccessTokenExpiration();
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(expiration))
                .claim("type", "access")
                .signWith(Keys.hmacShaKeyFor(tokenProperties.getSecretKey().getBytes()))
                .compact();
        return new TokenInfo(token, expiration);
    }

    public TokenInfo generateRefreshToken(String username) {
        long expiration = System.currentTimeMillis() + tokenProperties.getRefreshTokenExpiration();
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(expiration))
                .claim("type", "refresh")
                .signWith(Keys.hmacShaKeyFor(tokenProperties.getSecretKey().getBytes()))
                .compact();
        return new TokenInfo(token, expiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(tokenProperties.getSecretKey().getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractType(token));
    }
}
