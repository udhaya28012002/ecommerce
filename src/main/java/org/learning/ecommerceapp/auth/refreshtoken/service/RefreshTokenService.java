package org.learning.ecommerceapp.auth.refreshtoken.service;

import org.learning.ecommerceapp.config.TokenProperties;
import org.learning.ecommerceapp.auth.exception.InvalidTokenException;
import org.learning.ecommerceapp.auth.refreshtoken.entity.RefreshToken;
import org.learning.ecommerceapp.auth.refreshtoken.repository.RefreshTokenRepo;
import org.learning.ecommerceapp.auth.util.JWTUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepo refreshTokenRepo;
    private final JWTUtil jwtUtil;
    private final TokenProperties tokenProperties;

    public RefreshTokenService(RefreshTokenRepo refreshTokenRepo, JWTUtil jwtUtil, TokenProperties tokenProperties) {
        this.refreshTokenRepo = refreshTokenRepo;
        this.jwtUtil = jwtUtil;
        this.tokenProperties = tokenProperties;
    }

    public boolean validateRefreshToken(String token){
        return refreshTokenRepo.existsById(token);
    }

    public String createRefreshToken(String header, String ipAddr, String username){

        String generatedToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken(generatedToken);
        refreshToken.setExpiryDate(Instant.now().plus(tokenProperties.getRefreshTokenExpiration(), ChronoUnit.DAYS));
        refreshToken.setRevoked(false);
        refreshToken.setLastUsedAt(Instant.now());
        refreshToken.setDeviceInfo(header);
        refreshToken.setIpAddress(ipAddr);
        refreshToken.setUsername(username);

        refreshTokenRepo.save(refreshToken);

        return generatedToken;
    }

    @Transactional
    public String refreshToken(String refreshToken){

        RefreshToken token = refreshTokenRepo.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid Token"));

        if(token.getExpiryDate().isBefore(Instant.now()) ||
                token.isRevoked()){
            token.setRevoked(true);
            throw new InvalidTokenException("Token is expired");
        }

        return jwtUtil.generateJWTToken(token.getUsername());
    }

    @Transactional
    public void removeTokens(String refreshToken){
        RefreshToken token = refreshTokenRepo.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid Token"));
        token.setRevoked(true);
    }

}
