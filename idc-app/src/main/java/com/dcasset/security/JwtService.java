package com.dcasset.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与校验（HMAC-SHA256，无状态）
 */
@Component
public class JwtService {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final byte[] secretBytes;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtService(@Value("${app.security.jwt.secret}") String secret,
                      @Value("${app.security.jwt.access-token-validity}") long accessTokenValidity,
                      @Value("${app.security.jwt.refresh-token-validity}") long refreshTokenValidity) {
        // HS256 要求密钥不少于 256 bit
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        if (raw.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(raw, 0, padded, 0, raw.length);
            raw = padded;
        }
        this.secretBytes = raw;
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    public String createAccessToken(String username) {
        return sign(username, TYPE_ACCESS, accessTokenValidity);
    }

    public String createRefreshToken(String username) {
        return sign(username, TYPE_REFRESH, refreshTokenValidity);
    }

    public long getAccessTokenValidity() {
        return accessTokenValidity;
    }

    /**
     * 校验令牌并返回用户名；校验失败（签名/过期/类型不符）抛出异常
     */
    public String parse(String token, String expectedType) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secretBytes);
            if (!jwt.verify(verifier)) {
                throw new IllegalArgumentException("令牌签名无效");
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (claims.getExpirationTime() == null || claims.getExpirationTime().before(new Date())) {
                throw new IllegalArgumentException("令牌已过期");
            }
            if (!expectedType.equals(claims.getStringClaim("type"))) {
                throw new IllegalArgumentException("令牌类型不符");
            }
            return claims.getSubject();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("令牌解析失败");
        }
    }

    private String sign(String username, String type, long validitySeconds) {
        try {
            Date now = new Date();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(username)
                    .claim("type", type)
                    .issueTime(now)
                    .expirationTime(new Date(now.getTime() + validitySeconds * 1000))
                    .build();
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            JWSSigner signer = new MACSigner(secretBytes);
            jwt.sign(signer);
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("签发令牌失败", e);
        }
    }
}
