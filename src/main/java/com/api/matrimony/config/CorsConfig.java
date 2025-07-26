package com.api.matrimony.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.extern.slf4j.Slf4j;

/**
 * CORS Configuration
 * Handles Cross-Origin Resource Sharing settings for the application
 */
@Configuration
@EnableWebMvc
@Slf4j
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8081,http://127.0.0.1:3000,http://localhost:9999}")
    private String[] allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String[] allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:false}")  // Changed to false to avoid CORS issue
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("Configuring CORS with origins: {}", String.join(", ", allowedOrigins));
        
        // For development - allow all origins without credentials
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // Use allowedOriginPatterns instead of allowedOrigins
                .allowedMethods(allowedMethods)
                .allowedHeaders("*")
                .allowCredentials(false)  // Set to false for development
                .maxAge(maxAge);
        
        // Special mapping for authentication endpoints with specific origins
        registry.addMapping("/api/v1/auth/**")
                .allowedOrigins(allowedOrigins)  // Specific origins only
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)  // Allow credentials for specific origins
                .maxAge(maxAge);
    }
}