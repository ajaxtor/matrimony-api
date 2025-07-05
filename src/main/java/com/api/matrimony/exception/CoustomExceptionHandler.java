package com.api.matrimony.exception;



public class CoustomExceptionHandler {

	private CoustomExceptionHandler() {
		throw new IllegalArgumentException();
	}

	public static void throwBusinessException(ErrorEnum errorEnum) {
		throw new BusinessException(new CoustomError(errorEnum));
	}

}
