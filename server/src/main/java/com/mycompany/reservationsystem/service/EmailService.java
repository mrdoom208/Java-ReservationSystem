package com.mycompany.reservationsystem.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "spring.mail.host")
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @PostConstruct
    public void init() {
    }

    public void sendEmail(String to, String subject, String text) {
        if (mailSender == null) {
            return;
        }
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(System.getenv().getOrDefault("MAIL_USERNAME", "noreply@reservationsystem.com"));
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
