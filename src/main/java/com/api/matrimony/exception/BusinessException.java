package com.api.matrimony.exception;


import lombok.Data;

@Data
public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private String endPoint;
	private CoustomError coustomErrorDescription;

	public BusinessException(CoustomError messagError) {
		super(messagError.getExceptionError());
		this.endPoint = null;
		this.coustomErrorDescription = messagError;
	}

	public BusinessException(String message) {
		super(message);
	}

}
