package com.api.matrimony.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.api.matrimony.serviceImpl.UserServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT Authentication Filter
 * Processes JWT tokens for each request and sets up Spring Security context
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserServiceImpl userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        log.debug("JWT Filter - Processing request: {} {}", method, requestPath);

        // Skip JWT validation for public endpoints
        if (isPublicEndpoint(requestPath)) {
            log.debug("JWT Filter - Skipping JWT validation for public endpoint: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token from Authorization header
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(token);
                log.debug("JWT Filter - Extracted username from token: {}", username);
            } catch (Exception e) {
                log.error("JWT Filter - Token validation error for path {}: {}", requestPath, e.getMessage());
            }
        } else {
            log.debug("JWT Filter - No Authorization header found for path: {}", requestPath);
        }

        // Authenticate user if token is valid and no authentication exists
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userService.loadUserByUsername(username);

                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("JWT Filter - Successfully authenticated user: {} for path: {}", username, requestPath);
                } else {
                    log.warn("JWT Filter - Token validation failed for user: {} on path: {}", username, requestPath);
                }
            } catch (Exception e) {
                log.error("JWT Filter - Error setting user authentication for path {}: {}", requestPath, e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check if the request path is a public endpoint that doesn't require authentication
     * @param path The request path to check
     * @return true if the path is public, false otherwise
     */
    private boolean isPublicEndpoint(String path) {
        boolean isPublic = path.startsWith("/api/v1/auth/") ||
               path.startsWith("/auth/") ||  // Fallback pattern
               path.startsWith("/api/v1/public/") ||
               path.startsWith("/public/") ||
               path.startsWith("/health/") ||
               path.startsWith("/actuator/") ||
               path.equals("/api/v1/subscriptions/plans") ||
               path.equals("/subscriptions/plans") ||
               path.equals("/error") ||
               path.equals("/favicon.ico") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/api/v1/admin/login");  // <-- Added login path here

        if (isPublic) {
            log.debug("JWT Filter - Path {} identified as public endpoint", path);
        }

        return isPublic;
    }

    /**
     * Skip filter for OPTIONS requests (CORS preflight) and login endpoint
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        boolean skipFilter = "OPTIONS".equalsIgnoreCase(request.getMethod()) || 
                             path.equals("/api/v1/admin/login"); // <-- skip login endpoint
        if (skipFilter) {
            log.debug("JWT Filter - Skipping filter for request: {} {}", request.getMethod(), path);
        }
        return skipFilter;
    }
}
