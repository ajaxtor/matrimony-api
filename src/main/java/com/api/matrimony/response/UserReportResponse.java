package com.api.matrimony.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Report Response DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserReportResponse {
    
    private Long id;
    private Long reporterId;
    private Long reportedUserId;
    private String reporterName;
    private String reportedUserName;
    private String reason;
    private String description;
    private String status;
    private String adminNotes;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime updatedAt;
}
