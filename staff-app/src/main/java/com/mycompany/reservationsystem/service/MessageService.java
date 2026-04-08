package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MessageService {

    public static List<Map<String, Object>> getAllMessages() {
        return ApiClient.getMessages();
    }

    public static List<Message> getAllMessagesList() {
        List<Map<String, Object>> response = ApiClient.getMessages();
        List<Message> messages = new ArrayList<>();
        for (Map<String, Object> map : response) {
            Message msg = new Message();
            msg.setId(Long.valueOf(((Number) map.getOrDefault("id", 0)).longValue()));
            msg.setType((String) map.get("type"));
            msg.setMessageLabel((String) map.get("messageLabel"));
            msg.setMessageDetails((String) map.get("messageDetails"));
            messages.add(msg);
        }
        return messages;
    }

    public static Map<String, Object> createMessage(Object message) {
        return ApiClient.createMessage(message);
    }

    public static void updateMessage(Long id, Object message) {
        ApiClient.updateMessage(id, message);
    }

    public static long getUnreadCount() {
        return ApiClient.getUnreadMessagesCount();
    }

    public static void markAsRead(Long id) {
        ApiClient.markMessageAsRead(id);
    }

    public static Map<String, Object> sendSms(Object smsRequest) {
        return ApiClient.sendSms(smsRequest);
    }

    public static Optional<Message> findByLabel(String label) {
        List<Map<String, Object>> response = ApiClient.getMessages();
        for (Map<String, Object> map : response) {
            String itemLabel = (String) map.get("messageLabel");
            if (itemLabel != null && itemLabel.equals(label)) {
                Message msg = new Message();
                msg.setId(((Number) map.getOrDefault("id", 0)).longValue());
                msg.setType((String) map.get("type"));
                msg.setMessageLabel(itemLabel);
                msg.setMessageDetails((String) map.get("messageDetails"));
                return Optional.of(msg);
            }
        }
        return Optional.empty();
    }

    public static void createIfMissing(String label) {
        createIfMissing(label, "This message is auto generated for " + label);
    }

    public static void createIfMissing(String label, String defaultMessage) {
        if (findByLabel(label).isEmpty()) {
            Message msg = new Message();
            msg.setMessageLabel(label);
            msg.setType("AUTO");
            msg.setMessageDetails(defaultMessage);
            ApiClient.createMessage(msg);
        }
    }

    public static void seedMessagesFromBackend(List<String> labels) {
        ApiClient.seedMessages(labels);
    }

    public static void deleteMessage(Long id) {
        Map<String, Object> msgData = Map.of("id", id);
        ApiClient.updateMessage(id, msgData);
    }

    public static void saveMessage(Long id, String label, String message) {
        Map<String, Object> msgData = Map.of(
            "id", id,
            "messageLabel", label,
            "messageDetails", message
        );
        ApiClient.updateMessage(id, msgData);
    }
}
