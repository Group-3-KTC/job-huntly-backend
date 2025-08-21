package com.jobhuntly.backend.security.jwt;

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
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

@Component
public class JwtUtil {

    private final SecretKey key;

    @Getter
    private final long expirationMillis;

    @Getter
    private final String issuer;

    public JwtUtil(JwtProperties props) {
        // Validate props + fields bắt buộc
        String secret = requireNonBlank(props.getSecret(), "Missing security.jwt.secret (JWT_SECRET_KEY)");
        Duration expiry = Objects.requireNonNull(props.getExpirySeconds(), "Missing security.jwt.expiry-seconds");
        this.issuer = requireNonBlank(props.getIssuer(), "Missing security.jwt.issuer");

        // Hỗ trợ cả Base64 và plain text
        byte[] keyBytes;
        String s = secret.trim();
        if (looksLikeBase64(s)) {
            try {
                keyBytes = Decoders.BASE64.decode(s);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("JWT secret looks like Base64 but cannot be decoded.", e);
            }
        } else {
            keyBytes = s.getBytes(StandardCharsets.UTF_8);
        }

        // Đảm bảo đủ độ dài cho HS256
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret too short for HS256 (need >= 32 bytes after decoding).");
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMillis = expiry.getSeconds() * 1000L;
    }

    public String generateToken(String subject, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subject)
                .setIssuer(issuer)
                .claim("role", role)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenValid(String token, String expectedUsername) {
        try {
            Claims claims = extractAllClaims(token);
            String subject = claims.getSubject();
            if (subject == null || (expectedUsername != null && !subject.equals(expectedUsername))) {
                return false;
            }
            return !isTokenExpired(claims.getExpiration());
        } catch (JwtException ex) {
            // gồm ExpiredJwtException, MalformedJwtException, SignatureException...
            return false;
        }
    }

    public long getExpirationSeconds() {
        return expirationMillis / 1000L;
    }


    private Claims extractAllClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer(issuer)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(Date expiration) {
        return expiration == null || expiration.before(new Date());
    }

    private static String requireNonBlank(String v, String message) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalStateException(message);
        }
        return v.trim();
    }

    private static boolean looksLikeBase64(String s) {
        // Base64 hợp lệ: chỉ A–Z, a–z, 0–9, +, / và padding '='; độ dài bội số của 4
        return s.length() % 4 == 0 && s.matches("^[A-Za-z0-9+/]+={0,2}$");
    }
}
