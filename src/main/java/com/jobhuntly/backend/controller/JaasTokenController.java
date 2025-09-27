package com.jobhuntly.backend.controller;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("${backend.prefix}/meet")
@RequiredArgsConstructor
public class JaasTokenController {

    @Value("${jaas.appId}")
    private String appId;
    @Value("${jaas.kid}")
    private String kid;
    @Value("${jaas.privateKeyPemB64:}")
    private String privateKeyPemB64; // ưu tiên B64
    @Value("${jaas.privateKeyPem:}")
    private String privateKeyPemRaw; // fallback (optional)

    public record TokenReq(String room, String name, String email, String avatar, Boolean moderator) {
    }

    public record TokenRes(String jwt) {
    }

    @PostMapping("/token")
    public TokenRes create(@RequestBody TokenReq req) throws Exception {
        long now = System.currentTimeMillis() / 1000;

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID(appId + "/" + kid) // <APP_ID>/<KID>
                .build();

        Map<String, Object> ctxUser = new HashMap<>();
        ctxUser.put("name", Optional.ofNullable(req.name()).orElse("Guest"));
        ctxUser.put("email", req.email());
        ctxUser.put("avatar", req.avatar());
        ctxUser.put("moderator", Boolean.TRUE.equals(req.moderator()));

        Map<String, Object> features = Map.of(
                "recording", true,
                "livestreaming", false,
                "transcription", false,
                "outbound-call", false);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .audience("jitsi")
                .issuer(appId)
                .subject(appId)
                .issueTime(new Date(now * 1000))
                .notBeforeTime(new Date(now * 1000))
                .expirationTime(new Date((now + 2 * 60 * 60) * 1000))
                .claim("room", (req.room() == null || req.room().isBlank()) ? "*" : req.room())
                .claim("context", Map.of("user", ctxUser, "features", features))
                .build();

        SignedJWT jwt = new SignedJWT(header, claims);
        RSAPrivateKey pk = (RSAPrivateKey) readPrivateKeyFromEnv();
        jwt.sign(new RSASSASigner(pk));

        return new TokenRes(jwt.serialize());
    }

    private PrivateKey readPrivateKeyFromEnv() throws Exception {
        if (privateKeyPemB64 != null && !privateKeyPemB64.isBlank()) {
            byte[] pemBytes = Base64.getDecoder().decode(privateKeyPemB64.trim());
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(pemBytes));
        }
        if (privateKeyPemRaw != null && !privateKeyPemRaw.isBlank()) {
            String clean = privateKeyPemRaw
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] pkcs8 = Base64.getDecoder().decode(clean);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
        }
        throw new IllegalStateException("No JAAS private key configured");
    }
}
