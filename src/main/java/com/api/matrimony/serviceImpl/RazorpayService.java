package com.api.matrimony.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.api.matrimony.request.RazorpayOrderRequest;
import com.api.matrimony.response.RazorpayOrderResponse;

@Service
public class RazorpayService {

    private static final String RAZORPAY_API_KEY = "rzp_test_R8Qu5t36pGhI5U";
    private static final String RAZORPAY_API_SECRET = "4KKgoDG2ejRMoWBwiCvmzX7v";
    private static final String RAZORPAY_ORDER_URL = "https://api.razorpay.com/v1/orders";

    private final RestTemplate restTemplate;

    @Autowired
    public RazorpayService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RazorpayOrderResponse createOrder(RazorpayOrderRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Set Basic Auth Header
        String auth = RAZORPAY_API_KEY + ":" + RAZORPAY_API_SECRET;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        headers.set("Authorization", "Basic " + new String(encodedAuth));

        HttpEntity<RazorpayOrderRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<RazorpayOrderResponse> response = restTemplate.exchange(
                RAZORPAY_ORDER_URL,
                HttpMethod.POST,
                entity,
                RazorpayOrderResponse.class
        );

        return response.getBody();
    }
}
