package com.api.matrimony.exception;

import lombok.Data;

@Data
public class CoustomError {

	private String exceptionError;

	public CoustomError(ErrorEnum errorEnum) {
		this.exceptionError = errorEnum.getExceptionError();
	}

}
