package com.mycompany.reservationsystem.service;


import com.mycompany.reservationsystem.util.PhoneFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class SmsService {

    private final RestTemplate restTemplate;

    @Value("${philsms.api.token:}")
    private String apiToken;

    @Value("${philsms.sender.id:}")
    private String senderId;


    private static final String SMS_URL = "https://dashboard.philsms.com/api/v3/sms/send";

    public SmsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String sendSms(String recipient, String message) {
        if (apiToken == null || apiToken.isBlank()) {
            return "SMS service not configured - no API token";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiToken);

            String recipientphone = PhoneFormatter.normalizePHNumber(recipient);
            System.out.println(recipientphone);


            String body = String.format(
                    "{\"recipient\":\"%s\",\"sender_id\":\"%s\",\"type\":\"plain\",\"message\":\"%s\"}",
                    recipientphone, senderId, message.replace("\"", "\\\"").replace("\n", "\\n")
            );
            System.out.println(body);

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(SMS_URL, request, String.class);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            return "PhilSMS Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
        } catch (Exception e) {
            return "Unexpected error: " + e.getMessage();
        }
    }
}
