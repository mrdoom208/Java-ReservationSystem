package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.Config.AppSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MessageDispatchService {

    private static final Logger logger = LoggerFactory.getLogger(MessageDispatchService.class);

    private final SmsService smsService;

    public MessageDispatchService(SmsService smsService) {
        this.smsService = smsService;
    }

    public String send(String phone, String message) throws Exception {
        logger.info("Sending SMS to {} via PhilSMS API", phone);
        return smsService.sendSms(phone, message);
    }
}
