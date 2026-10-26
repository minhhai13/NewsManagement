package com.minhhai.newsmanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:NewsManagementSuperSecretKeyForJwtAuthenticationWith256BitsMinimumLength2026}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /** Trả về Claims nếu token hợp lệ và chưa hết hạn, ngược lại trả về null. */
    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
