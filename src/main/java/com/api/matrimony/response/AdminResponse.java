package com.api.matrimony.response;

import java.time.LocalDateTime;

import org.hibernate.validator.constraints.ISBN;

import com.api.matrimony.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private String email;
    private String name;
    private String phone;
    private boolean isActive;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime lastLogin;
}
