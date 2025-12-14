package com.freemix.freemix.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "freemix_qr_login_secret_key_please_change_to_config_256";

    private static Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateQrSessionToken(String sessionId, long ttlSeconds) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(ttlSeconds);
        return Jwts.builder()
                .setSubject(sessionId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static boolean validateQrSessionToken(String token, String expectedSessionId) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String subject = claims.getSubject();
            Date expiration = claims.getExpiration();
            if (expiration == null || expiration.before(new Date())) {
                return false;
            }
            return expectedSessionId.equals(subject);
        } catch (Exception e) {
            return false;
        }
    }
}

