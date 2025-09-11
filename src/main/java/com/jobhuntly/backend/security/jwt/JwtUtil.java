package com.jobhuntly.backend.security.jwt;

import com.jobhuntly.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
@Getter
public class JwtUtil {

    public static final String CLAIM_TYP = "typ";
    public static final String TYP_ACCESS = "access";
    public static final String TYP_REFRESH = "refresh";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_USERID = "userId";
    public static final String CLAIM_VERSION = "v";

    private final SecretKey key;
    private final String issuer;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtUtil(JwtProperties props) {
        this.key = buildKey(props.getSecret());
        this.issuer = props.getIssuer();
        this.accessTtl = props.getAccessTtl();
        this.refreshTtl = props.getRefreshTtl();
    }

    private SecretKey buildKey(String secretBase64OrRaw) {
        try {
            byte[] bytes;
            try {
                bytes = Decoders.BASE64.decode(secretBase64OrRaw);
            } catch (IllegalArgumentException e) {
                bytes = secretBase64OrRaw.getBytes(StandardCharsets.UTF_8);
            }
            return Keys.hmacShaKeyFor(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JWT secret key", e);
        }
    }

    public String issueAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuer(issuer)
                .claim(CLAIM_ROLE, user.getRole())
                .claim(CLAIM_USERID, user.getId())
                .claim(CLAIM_TYP, TYP_ACCESS)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(accessTtl)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String issueRefreshToken(Long userId, int version) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuer(issuer)
                .claim(CLAIM_VERSION, version)
                .claim(CLAIM_TYP, TYP_REFRESH)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(refreshTtl)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseAndValidate(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey((Key) key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isAccess(Claims c)  { return TYP_ACCESS.equals(c.get(CLAIM_TYP)); }
    public boolean isRefresh(Claims c) { return TYP_REFRESH.equals(c.get(CLAIM_TYP)); }

    public Long userIdFromClaims(Claims c) {
        Object v = c.get(CLAIM_USERID);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        return Long.valueOf(String.valueOf(v));
    }

    public Integer versionFromClaims(Claims c) {
        Object v = c.get(CLAIM_VERSION);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        return Integer.valueOf(String.valueOf(v));
    }
}
