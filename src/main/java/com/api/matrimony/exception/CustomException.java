package com.api.matrimony.exception;

//Custom Exception Classes

/**
* Custom Exception for business logic errors
*/

public class CustomException extends RuntimeException {
 
 public CustomException(String message) {
     super(message);
 }
 
 public CustomException(String message, Throwable cause) {
     super(message, cause);
 }
}
