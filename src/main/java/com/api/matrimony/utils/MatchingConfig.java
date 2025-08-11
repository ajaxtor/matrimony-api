package com.api.matrimony.utils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "app.matching")
@Data
public class MatchingConfig {
    private Map<String, Integer> weights = new HashMap<>();
    
    @PostConstruct
    public void init() {
        // Default weights if not configured
        weights.putIfAbsent("age", 15);
        weights.putIfAbsent("height", 10);
        weights.putIfAbsent("maritalStatus", 15);
        weights.putIfAbsent("religion", 12);
        weights.putIfAbsent("caste", 10);
        weights.putIfAbsent("education", 12);
        weights.putIfAbsent("occupation", 10);
        weights.putIfAbsent("income", 8);
        weights.putIfAbsent("location", 8);
    }
}