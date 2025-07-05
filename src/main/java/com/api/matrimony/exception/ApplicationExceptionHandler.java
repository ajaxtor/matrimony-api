package com.api.matrimony.exception;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<?> handleApplicationException(final ApplicationException exception,
			final HttpServletRequest request) {
		// log.error(String.format("Error GUID=%s; error message: %s", guid,
		// exception.getMessage()), exception);
		var response = new ApiErrorResponse(exception.getErrorCode(), exception.getMessage(),
				exception.getHttpStatus().value(), Boolean.FALSE, request.getMethod(), LocalDateTime.now());
		return new ResponseEntity<>(response, exception.getHttpStatus());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleUnknownException(final Exception exception, final HttpServletRequest request) {
		var guid = UUID.randomUUID().toString();
//      log.error(
//            String.format("Error GUID=%s; error message: %s", guid, exception.getMessage()), 
////            exception
////        );  
		var response = new ApiErrorResponse("500", exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
				Boolean.FALSE, request.getMethod(), LocalDateTime.now());
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}