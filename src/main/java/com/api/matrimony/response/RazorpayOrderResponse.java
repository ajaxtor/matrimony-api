package com.api.matrimony.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderResponse {
	
 private String id;
 private String entity;
 private Integer amount;
 private String currency;
 private String status;
 private String receipt;
 private Integer amountPaid;
 private Integer amountDue;
 private String offerId;
 private Integer attempts;
 private Object notes; 
 private Long created_at;

}


