package com.api.matrimony;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class SmsService {

    @Value("${msg91.authkey}")
    private String authKey;

    @Value("${msg91.sender}")
    private String senderId;

    @Value("${msg91.templateId}")
    private String templateId;

    @Value("${msg91.url}")
    private String msg91Url;

    /**
     * Send OTP via MSG91 Flow API
     */
    public String sendSms(String mobile, String otp) {
        RestTemplate restTemplate = new RestTemplate();

        // ✅ Ensure mobile has country code
        if (!mobile.startsWith("91")) {
            mobile = "91" + mobile;
        }

        // ✅ Ensure senderId is uppercase & 6 chars
        String validSender = senderId.toUpperCase().trim();
        if (validSender.length() != 6) {
            throw new IllegalArgumentException("Sender ID must be exactly 6 uppercase characters. Found: " + validSender);
        }

        // ✅ Build payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("template_id", templateId);
        payload.put("sender", validSender);
        payload.put("short_url", "0");

        List<Map<String, Object>> recipients = new ArrayList<>();
        Map<String, Object> recipient = new HashMap<>();
        recipient.put("mobiles", mobile);
        recipient.put("OTP", otp); // must match ##OTP## in template
        recipients.add(recipient);

        payload.put("recipients", recipients);

        // ✅ Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authkey", authKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        // ✅ Call MSG91 API
        ResponseEntity<String> response =
                restTemplate.exchange(msg91Url, HttpMethod.POST, entity, String.class);

        // ✅ Log for debugging
        System.out.println("SMS Request Payload: " + payload);
        System.out.println("MSG91 Response: " + response.getBody());

        return response.getBody();
    }
}
