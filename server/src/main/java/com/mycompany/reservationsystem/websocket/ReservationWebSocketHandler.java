package com.mycompany.reservationsystem.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.reservationsystem.dto.WebUpdateDTO;
import com.mycompany.reservationsystem.model.Notification;
import com.mycompany.reservationsystem.repository.NotificationRepository;
import com.mycompany.reservationsystem.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ReservationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReservationWebSocketHandler.class);
    
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        logger.info("Frontend WebSocket connected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        logger.info("Received message from frontend: {}", message.getPayload());
        
        try {
            WebUpdateDTO dto = objectMapper.readValue(message.getPayload(), WebUpdateDTO.class);
            String code = dto.getCode();
            
            if ("TABLE_READY".equals(code)) {
                logger.info("TABLE_READY notification for: " + dto.getReference());
                
                Notification notification = new Notification();
                notification.setAccount(dto.getReference());
                notification.setCode("TABLE_READY");
                notification.setMessage(dto.getMessage() != null ? dto.getMessage() : "Your table is ready!");
                notification.setTimestamp(System.currentTimeMillis());
                notification.setSent(false);
                
                notificationRepository.save(notification);
                logger.info("Notification saved to DB for: " + dto.getReference());
                
                messagingTemplate.convertAndSend("/topic/account." + dto.getReference(), notification);
                logger.info("TABLE_READY broadcasted to /topic/account." + dto.getReference());
                
                broadcast(dto);
                logger.info("TABLE_READY also broadcasted via raw WebSocket");
            }
            
        } catch (Exception e) {
            logger.error("Failed to process WebSocket message: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        logger.info("Frontend WebSocket disconnected: {}", session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        sessions.remove(session);
    }

    public void broadcast(WebUpdateDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            TextMessage message = new TextMessage(json);
            
            sessions.removeIf(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                        return false;
                    }
                } catch (IOException e) {
                    logger.warn("Failed to send to session {}: {}", session.getId(), e.getMessage());
                }
                return true;
            });
            
            logger.info("Broadcast message to {} sessions: {}", sessions.size(), json);
        } catch (IOException e) {
            logger.error("Failed to serialize WebSocket message: {}", e.getMessage());
        }
    }

    public void broadcastMessage(String message) {
        TextMessage textMessage = new TextMessage(message);
        
        sessions.removeIf(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                    return false;
                }
            } catch (IOException e) {
                logger.warn("Failed to send to session {}: {}", session.getId(), e.getMessage());
            }
            return true;
        });
        
        logger.info("Broadcast raw message to {} sessions", sessions.size());
    }

    public int getConnectedCount() {
        return sessions.size();
    }
}