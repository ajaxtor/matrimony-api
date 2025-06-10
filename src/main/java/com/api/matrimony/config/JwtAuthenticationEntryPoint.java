package com.api.matrimony.config;

import java.io.IOException;
import java.time.Instant;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT Authentication Entry Point
 * Handles unauthorized access attempts and returns proper JSON error response
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        log.error("Unauthorized access attempt: {} for path: {}", authException.getMessage(), request.getRequestURI());
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String jsonResponse = """
            {
                "success": false,
                "error": "Unauthorized",
                "message": "Access token is missing or invalid",
                "status": 401,
                "timestamp": "%s",
                "path": "%s"
            }
            """.formatted(Instant.now().toString(), request.getRequestURI());
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}