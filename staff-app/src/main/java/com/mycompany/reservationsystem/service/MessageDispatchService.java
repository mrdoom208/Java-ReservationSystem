package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.api.ApiClient;

import java.util.Map;

public class MessageDispatchService {

    public static Map<String, Object> sendSms(Object smsRequest) {
        return ApiClient.sendSms(smsRequest);
    }

    public static void send(String phone, String message) {
        Map<String, Object> smsRequest = Map.of(
            "phoneNumber", phone,
            "message", message
        );
        ApiClient.sendSms(smsRequest);
    }
}
