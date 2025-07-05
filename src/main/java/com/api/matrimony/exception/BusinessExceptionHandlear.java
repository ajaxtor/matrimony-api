package com.api.matrimony.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.api.matrimony.response.APIResonse;

public class BusinessExceptionHandlear extends ResponseEntityExceptionHandler {

	@ExceptionHandler(value = { BusinessException.class })
	public ResponseEntity<Object> businessRuntimeException(BusinessException ex) {
		APIResonse<Object> errorResonse = new APIResonse<>();
		errorResonse.setError("Error", 500);
		errorResonse.setErrorDescription(ex.getCoustomErrorDescription());
		errorResonse.setStatus(Boolean.FALSE);
		return new ResponseEntity<>(errorResonse, HttpStatus.OK);
	}

	protected ResponseEntity<Object> hendleExceptionInternul(Exception ex, Object body, HttpHeaders headers,
			WebRequest request) {
		APIResonse<Object> errorResonse = new APIResonse<>();
		errorResonse.setError("Error", 500);
		CoustomError coustomError = new CoustomError(ErrorEnum.BAD_RESQUEST);
		errorResonse.setErrorDescription(coustomError);
		errorResonse.setStatus(Boolean.FALSE);
		return new ResponseEntity<>(errorResonse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
