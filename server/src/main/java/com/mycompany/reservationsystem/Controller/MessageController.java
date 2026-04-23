package com.mycompany.reservationsystem.Controller;

import com.mycompany.reservationsystem.model.Message;
import com.mycompany.reservationsystem.repository.MessageRepository;
import com.mycompany.reservationsystem.service.MessageService;
import com.mycompany.reservationsystem.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private MessageRepository messageRepository;

    @PostMapping
    public Message createMessage(@RequestBody Message message) {
        return messageRepository.save(message);
    }

    @PostMapping("/all")
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    @PostMapping("/get")
    public ResponseEntity<Message> getMessageById(@RequestBody IdRequest request) {
        return messageRepository.findById(request.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Message> updateMessage(@PathVariable Long id, @RequestBody Message messageDetails) {
        return messageRepository.findById(id)
                .map(message -> {
                    message.setMessageLabel(messageDetails.getMessageLabel());
                    message.setMessageDetails(messageDetails.getMessageDetails());
                    message.setRead(messageDetails.isRead());
                    Message updated = messageRepository.save(message);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        if (messageRepository.existsById(id)) {
            messageRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().<Void>build();
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody Map<String, String> request) {
        try {
            String phoneNumber = request.get("phoneNumber");
            String message = request.get("message");
            if (phoneNumber == null || message == null) {
                return ResponseEntity.badRequest().body("phoneNumber and message are required");
            }
            smsService.sendSms(phoneNumber, message);
            return ResponseEntity.ok("Message sent successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send message: " + e.getMessage());
        }
    }

    @PostMapping("/by-label")
    public List<Message> getMessagesByLabel(@RequestBody LabelRequest request) {
        return messageRepository.findByMessageLabelContainingIgnoreCase(request.getLabel());
    }

    @PostMapping("/unread/count")
    public long getUnreadCount() {
        return messageRepository.findAll().stream().filter(m -> !m.isRead()).count();
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        return messageRepository.findById(id)
                .<ResponseEntity<Void>>map(message -> {
                    message.setRead(true);
                    messageRepository.save(message);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public static class IdRequest {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    public static class LabelRequest {
        private String label;
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    @PostMapping("/seed")
    public ResponseEntity<String> seedMessages(@RequestBody List<String> labels) {
        try {
            for (String label : labels) {
                messageService.createIfMissing(label, "This message is auto generated for " + label);
            }
            return ResponseEntity.ok("Messages seeded successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to seed messages: " + e.getMessage());
        }
    }
}
