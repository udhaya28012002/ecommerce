package org.learning.ecommerceapp.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.learning.ecommerceapp.auth.dto.AuthResDto;
import org.learning.ecommerceapp.auth.dto.RefreshTokenInput;
import org.learning.ecommerceapp.auth.refreshtoken.service.RefreshTokenService;
import org.learning.ecommerceapp.user.dto.request.LoginReqDto;
import org.learning.ecommerceapp.auth.util.JWTUtil;
import org.learning.ecommerceapp.user.dto.request.UserCreationDto;
import org.learning.ecommerceapp.user.dto.response.UserResDto;
import org.learning.ecommerceapp.user.service.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final RegistrationService registrationService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final JWTUtil jwtUtil;

    public AuthController(RegistrationService registrationService, AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService, JWTUtil jwtUtil) {
        this.registrationService = registrationService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResDto> generateJWTToken(@Valid @RequestBody LoginReqDto loginReqDto, HttpServletRequest request) {

        log.debug("Authentication request received for username: {}", loginReqDto.getUsername());

        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginReqDto.getUsername(),
                            loginReqDto.getPassword())
            );

            log.info("User authenticated successfully: {}", loginReqDto.getUsername());

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String accessToken = null;
            String refreshToken = null;

            String header = request.getHeader("User-Agent");
            String ipAddress = request.getRemoteAddr();

            if (userDetails != null) {

                log.info("Generating JWT and Refresh token for user: {}", userDetails.getUsername());

                accessToken = jwtUtil.generateJWTToken(
                        userDetails.getUsername(),
                        userDetails.getAuthorities().toString());

                refreshToken = refreshTokenService.createRefreshToken(
                        header,
                        ipAddress,
                        userDetails.getUsername(),
                        userDetails.getAuthorities().toString());
            }

            AuthResDto authResDto = new AuthResDto();
            authResDto.setAccessToken(accessToken);
            authResDto.setRefreshToken(refreshToken);

            log.info("Authentication process completed successfully for user: {}", loginReqDto.getUsername());

            return ResponseEntity.ok(authResDto);

        } catch (Exception e) {

            log.error("Authentication failed for user: {}", loginReqDto.getUsername(), e);

            throw e;
        }
    }

    @PostMapping("/createUser")
    public ResponseEntity<AuthResDto> createUser(@Valid @RequestBody UserCreationDto userCreationInfo, HttpServletRequest request) {

        log.debug("User registration request received for username: {}", userCreationInfo.getUserName());

        UserResDto createdUser = registrationService.registerUser(userCreationInfo, false);

        log.info("User created successfully: {}", createdUser.getUserName());

        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            createdUser.getUserName(),
                            userCreationInfo.getPassword())
            );

            log.info("Auto login successful after registration for user: {}", createdUser.getUserName());

            String accessToken = null;
            String refreshToken = null;

            String header = request.getHeader("User-Agent");
            String ipAddress = request.getRemoteAddr();

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            if (userDetails != null) {

                log.info("Generating tokens for newly registered user: {}", userDetails.getUsername());

                accessToken = jwtUtil.generateJWTToken(
                        userDetails.getUsername(),
                        userDetails.getAuthorities().toString());

                refreshToken = refreshTokenService.createRefreshToken(
                        header,
                        ipAddress,
                        userDetails.getUsername(),
                        userDetails.getAuthorities().toString());
            }

            AuthResDto authResDto = new AuthResDto();
            authResDto.setAccessToken(accessToken);
            authResDto.setRefreshToken(refreshToken);

            log.info("Registration and authentication completed successfully for user: {}", createdUser.getUserName());

            return ResponseEntity.status(HttpStatus.CREATED).body(authResDto);

        } catch (Exception e) {

            log.error("Registration authentication failed for user: {}", createdUser.getUserName(), e);

            throw e;
        }
    }

    @PostMapping("/refreshAuth")
    public ResponseEntity<String> refreshAuth(@Valid @RequestBody RefreshTokenInput refreshToken) {

        log.debug("Refresh access token request received");

        String newAccessToken = refreshTokenService.refreshToken(refreshToken.getRefreshToken());

        log.info("Access token refreshed successfully");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(newAccessToken);
    }

    @PostMapping("/deleteRefreshAuth")
    public void deleteRefreshAuth(@Valid @RequestBody RefreshTokenInput refreshToken) {

        log.debug("Refresh token revoke request received");

        refreshTokenService.removeTokens(refreshToken.getRefreshToken());

        log.info("Refresh token revoked successfully");
    }
}