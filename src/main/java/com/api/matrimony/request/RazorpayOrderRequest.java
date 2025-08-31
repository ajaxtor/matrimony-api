package com.api.matrimony.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderRequest {
	
	private Integer amount;
	private String currency;
	private String receipt;

}
