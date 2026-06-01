package org.learning.ecommerceapp.auth.filters;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.learning.ecommerceapp.auth.util.JWTUtil;
import org.learning.ecommerceapp.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JWTAuthFilter.class);

    private final JWTUtil jwtUtil;
    private final UserService userService;

    public JWTAuthFilter(JWTUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.equals("/api/authenticate")
                || path.equals("/api/createUser")
                || path.equals("/api/refreshAuth");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        log.debug("JWT filter invoked for path: {}", request.getServletPath());

        if (request.getServletPath().equals("/api/refreshAuth")) {

            log.info("Skipping JWT validation for refresh token endpoint");

            filterChain.doFilter(request, response);
            return;
        }

        String token = null;
        String username = null;

        try {

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

                log.debug("Authorization header found");

                token = authorizationHeader.substring(7);
                username = jwtUtil.extractUsernameFromToken(token);

                log.debug("Username extracted from token: {}", username);

            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                log.debug("Loading user details for username: {}", username);

                UserDetails userDetails = userService.loadUserByUsername(username);

                if (jwtUtil.validateToken(username, userDetails, token)) {

                    log.debug("JWT token validated successfully for user: {}", username);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Security context updated for user: {}", username);
                } else {

                    log.warn("JWT token validation failed for user: {}", username);
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException ex) {

            log.warn("JWT token expired: {}", ex.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.setContentType("application/json");

            response.getWriter().write("""
                        {
                            "message": "Access token expired"
                        }
                    """);
        } catch (Exception ex){
            log.error("Unexpected error occurred in JWT filter", ex);
            throw ex;
        }
    }

    //Optional If there is no db hit is needed for every api validation:
    /*protected void doFilterInternal_Temp(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        String token = null;
        String username = null;
        List<Role> role = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            username = jwtUtil.extractUsernameFromToken(token);
            role = jwtUtil.extractRoleFromToken(token);
        }

        List<SimpleGrantedAuthority> authorities = role.stream()
                .map((r) -> new SimpleGrantedAuthority(r.name()))
                .toList();

        if (token != null && username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //UserDetails userDetails = userService.loadUserByUsername(username);

            if (jwtUtil.validateToken(username, userDetails, token)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);

    }*/

}
