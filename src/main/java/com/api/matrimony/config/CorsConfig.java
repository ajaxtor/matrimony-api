package com.api.matrimony.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class CorsConfig implements WebMvcConfigurer {

 @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8081}")
 private String[] allowedOrigins;

 @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
 private String[] allowedMethods;

 @Value("${app.cors.allowed-headers:*}")
 private String allowedHeaders;

 @Value("${app.cors.allow-credentials:true}")
 private boolean allowCredentials;

 @Override
 public void addCorsMappings(CorsRegistry registry) {
     registry.addMapping("/**")
             .allowedOrigins(allowedOrigins)
             .allowedMethods(allowedMethods)
             .allowedHeaders(allowedHeaders.split(","))
             .allowCredentials(allowCredentials)
             .maxAge(3600);
 }
}
