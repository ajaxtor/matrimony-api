package com.api.matrimony.exception;


/**
* Unauthorized Access Exception
*/

public class UnauthorizedException extends RuntimeException {
 
 public UnauthorizedException(String message) {
     super(message);
 }
 
 public UnauthorizedException(String message, Throwable cause) {
     super(message, cause);
 }
}

