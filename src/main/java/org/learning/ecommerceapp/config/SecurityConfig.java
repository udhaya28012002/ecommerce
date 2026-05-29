package org.learning.ecommerceapp.config;

import org.learning.ecommerceapp.auth.exception.CustomAccessDeniedHandler;
import org.learning.ecommerceapp.auth.exception.CustomAuthenticationEntryPoint;
import org.learning.ecommerceapp.auth.filters.JWTAuthFilter;
import org.learning.ecommerceapp.user.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JWTAuthFilter jwtAuthFilter;

    private final PasswordEncoder passwordEncoder;

    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    public SecurityConfig(JWTAuthFilter jwtAuthFilter, PasswordEncoder passwordEncoder,
            CustomAccessDeniedHandler customAccessDeniedHandler,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.passwordEncoder = passwordEncoder;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers(
                                "/",
                                "/index.html",
                                "/cart.html",
                                "/orders.html",
                                "/products.html",
                                "/profile.html",
                                "/product-detail.html",
                                "/admin-dashboard.html",
                                "/admin-coupons.html",
                                "/admin-orders.html",
                                "/admin-users.html",
                                "/admin-products.html",
                                "/css/**",
                                "/js/**",
                                "/api/authenticate",
                                "/api/createUser",
                                "/api/refreshAuth").permitAll()
                                .anyRequest().authenticated())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler));

        httpSecurity.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserService userService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(daoAuthenticationProvider);
    }

}
