package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.dto.WebUpdateDTO;
import com.mycompany.reservationsystem.model.Customer;
import com.mycompany.reservationsystem.model.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SmsService smsService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${spring.mail.username}")
    private String gmailUsername;

    @Value("${spring.mail.password}")
    private String gmailPassword;

    /**
     * Sends SMS, Gmail, and WebSocket notification asynchronously.
     */
    @Async
    public void notifyCustomer(Customer customer, Reservation reservation, WebUpdateDTO dto) {
        try {
            // 1️⃣ SMS
            String smsDetails = "Hello " + customer.getName() +
                    ", your reservation has been successfully made.\n" +
                    "Reference: " + reservation.getReference() + "\n" +
                    "Party Size: " + reservation.getPax() + "\n" +
                    "We look forward to welcoming you!";

            String smsResponse = smsService.sendSms(customer.getPhone(), smsDetails);
            System.out.println("SMS Response: " + smsResponse);

            // 2️⃣ Gmail
            System.out.println(reservation.getCustomer().getEmail());
            System.out.println(!customer.getEmail().isBlank()&&!customer.getEmail().isEmpty());
            if(!customer.getEmail().isBlank()&&!customer.getEmail().isEmpty()) {

                String subject = "Reservation Confirmation | Ref: " + reservation.getReference();
                String emailBody = "Hi " + customer.getName() + ",\n\n" +
                        "Your reservation has been successfully made.\n" +
                        "Reference: " + reservation.getReference() + "\n" +
                        "Party Size: " + reservation.getPax() + "\n\n" +
                        "We look forward to welcoming you!\n\n" +
                        "Thank you!";

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(customer.getEmail());
                message.setSubject(subject);
                message.setText(emailBody);

                mailSender.send(message);
                System.out.println("Email sent to: " + customer.getEmail());

            }
            // 3️⃣ WebSocket
            messagingTemplate.convertAndSend("/topic/forms", dto);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
