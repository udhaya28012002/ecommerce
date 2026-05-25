package org.learning.ecommerceapp.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.learning.ecommerceapp.auth.dto.AuthResDto;
import org.learning.ecommerceapp.auth.refreshtoken.service.RefreshTokenService;
import org.learning.ecommerceapp.user.dto.request.LoginReqDto;
import org.learning.ecommerceapp.auth.util.JWTUtil;
import org.learning.ecommerceapp.user.dto.request.UserCreationDto;
import org.learning.ecommerceapp.user.dto.response.UserResDto;
import org.learning.ecommerceapp.user.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

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
    public ResponseEntity<AuthResDto> generateJWTToken(@RequestBody LoginReqDto loginReqDto, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginReqDto.getUsername(), loginReqDto.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String accessToken = null;
            String refreshToken = null;

            String header = request.getHeader("User-Agent");
            String ipAddress = request.getRemoteAddr();

            if (userDetails != null) {
                accessToken = jwtUtil.generateJWTToken(userDetails.getUsername());
                refreshToken = refreshTokenService.createRefreshToken(header, ipAddress, userDetails.getUsername());
            }

            AuthResDto authResDto = new AuthResDto();
            authResDto.setAccessToken(accessToken);
            authResDto.setRefreshToken(refreshToken);

            return ResponseEntity.ok(authResDto);

        } catch (Exception e) {
            System.out.println(e);
            throw e;
        }
    }



    @PostMapping("/createUser")
    public ResponseEntity<AuthResDto> createUser(@Valid @RequestBody UserCreationDto userCreationInfo, HttpServletRequest request) {

        UserResDto createdUser = registrationService.registerUser(userCreationInfo, false);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(createdUser.getUserName(), userCreationInfo.getPassword())
            );

            String accessToken = null;
            String refreshToken = null;

            String header = request.getHeader("User-Agent");
            String ipAddress = request.getRemoteAddr();

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            if (userDetails != null) {
                accessToken = jwtUtil.generateJWTToken(userDetails.getUsername());
                refreshToken = refreshTokenService.createRefreshToken(header, ipAddress, userDetails.getUsername());
            }

            AuthResDto authResDto = new AuthResDto();
            authResDto.setAccessToken(accessToken);
            authResDto.setRefreshToken(refreshToken);

            return ResponseEntity.status(HttpStatus.CREATED).body(authResDto);

        } catch (Exception e) {
            System.out.println(e);
            throw e;
        }
    }

    @PostMapping("/refreshAuth")
    public ResponseEntity<String> refreshAuth(@RequestParam String refreshToken){
        return ResponseEntity.status(HttpStatus.CREATED).body(refreshTokenService.refreshToken(refreshToken));
    }

    @PostMapping("/deleteRefreshAuth")
    public void deleteRefreshAuth(@RequestParam String refreshToken){
        refreshTokenService.removeTokens(refreshToken);
    }

}
