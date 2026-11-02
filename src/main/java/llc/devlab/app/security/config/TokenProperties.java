package llc.devlab.app.security.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.jwt")
@Getter
public class TokenProperties {

    private final String secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public TokenProperties(String secretKey,
                           long accessTokenExpiration,
                           long refreshTokenExpiration) {
        this.secretKey = secretKey;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
}
