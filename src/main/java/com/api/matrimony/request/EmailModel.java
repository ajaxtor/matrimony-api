package com.api.matrimony.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailModel {

	   private String to;
	    private String username;
	    private String verificationCode;
	
}
