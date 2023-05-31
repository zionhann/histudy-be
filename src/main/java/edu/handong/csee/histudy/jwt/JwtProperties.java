package edu.handong.csee.histudy.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@ConfigurationProperties(prefix = "custom.jwt")
public class JwtProperties {

    private final SecretKey key;
    private final String issuer;
    private final String accessTokenExpiry;
    private final String refreshTokenExpiry;

    @ConstructorBinding
    public JwtProperties(String secret, String issuer, String accessTokenExpiry, String refreshTokenExpiry) {
        this.issuer = issuer;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;

        byte[] decoded = Base64.getDecoder().decode(secret);
        this.key = new SecretKeySpec(decoded, "HmacSHA256");
    }

    public SecretKey getKey() {
        return key;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getTokenExpiry(GrantType typ) {
        return (typ.equals(GrantType.ACCESS_TOKEN))
                ? accessTokenExpiry
                : refreshTokenExpiry;
    }
}
