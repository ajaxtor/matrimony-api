package com.api.matrimony.response;

import com.api.matrimony.exception.CoustomError;

import lombok.Data;

@Data

public class APIResonse<T> {

	private String message;
	private Integer statusCode;
	private boolean status;
	private T data;
	private CoustomError errorDescription;

	public APIResonse() {
		this.statusCode = 200;
		this.setStatus(Boolean.TRUE);
		this.setMessage(message);
		this.errorDescription = null;
	}

	public void setError(String message, Integer statusCode) {
		this.message = message;
		this.statusCode = statusCode;
		this.errorDescription = null;
	}
}
