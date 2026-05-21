package org.learning.ecommerceapp.auth.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.learning.ecommerceapp.user.dto.request.LoginReqDto;
import org.learning.ecommerceapp.auth.util.JWTUtil;
import org.learning.ecommerceapp.user.dto.request.UserCreationDto;
import org.learning.ecommerceapp.user.dto.response.UserResDto;
import org.learning.ecommerceapp.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final JWTUtil jwtUtil;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<String> generateJWTToken(@RequestBody LoginReqDto loginReqDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginReqDto.getUsername(), loginReqDto.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String token = null;
            if (userDetails != null) {
                token = jwtUtil.generateJWTToken(userDetails.getUsername());
            }

            return ResponseEntity.ok(token);

        } catch (Exception e) {
            throw e;
        }
    }

    @PostMapping("/createUser")
    public ResponseEntity<String> createUser(@Valid @RequestBody UserCreationDto userCreationInfo) {

        UserResDto createdUser = userService.createUser(userCreationInfo, false);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(createdUser.getUserName(), userCreationInfo.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String token = null;
            if (userDetails != null) {
                token = jwtUtil.generateJWTToken(userDetails.getUsername());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(token);

        } catch (Exception e) {
            throw e;
        }
    }

}
