package org.learning.ecommerceapp.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.learning.ecommerceapp.config.TokenProperties;
import org.learning.ecommerceapp.user.enums.Role;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JWTUtil {

    private final TokenProperties tokenProperties;
    private final SecretKey key;

    public JWTUtil(TokenProperties tokenProperties) {
        this.tokenProperties = tokenProperties;
        this.key = Keys.hmacShaKeyFor(tokenProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateJWTToken(String username, String role) {

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenProperties.getAccessTokenExpiration()))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
    /*public String generateJWTToken(String username, List<Role> role) {

        return Jwts.builder()
                .subject(username)
                .claim("role", List.class)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenProperties.getAccessTokenExpiration()))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }*/

    public String extractUsernameFromToken(String token){
        return  extractPayload(token).getSubject();
    }

    public List<Role> extractRoleFromToken(String token){
        return extractPayload(token).get("role", List.class);
    }

    public boolean validateToken(String username, UserDetails userDetails, String token){
        return userDetails.getUsername().equalsIgnoreCase(username) && !isTokenExpired(token);
    }

    public boolean validateIfTokenIsExpired(String token){
        return isTokenExpired(token);
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
