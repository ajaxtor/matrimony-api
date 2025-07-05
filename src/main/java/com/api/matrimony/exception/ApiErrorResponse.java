package com.api.matrimony.exception;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class ApiErrorResponse {
	private final String errorCode;
	private final String message;
	private final Integer statusCode;
	private final boolean status;
	private final String method;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private final LocalDateTime timestamp;
}
