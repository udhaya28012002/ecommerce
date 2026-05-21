package org.learning.ecommerceapp.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class JWTUtil {

    private final String SECRET = "7xA8u9KzMvF3qR5tWbN2xY7zC9vB4mQ1pG6hJ8kL0mN";

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour

    public String generateJWTToken(String username) {

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsernameFromToken(String token){
        return  extractPayload(token).getSubject();
    }

    public boolean validateToken(String username, UserDetails userDetails, String token){
        return userDetails.getUsername().equalsIgnoreCase(username) && !isTokenExpired(token);
    }

    private Claims extractPayload(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token){
        return extractPayload(token).getExpiration().before(new Date()) ;
    }
}
