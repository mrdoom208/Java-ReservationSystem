package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.dto.WebUpdateDTO;
import com.mycompany.reservationsystem.websocket.ReservationWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketBroadcastService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketBroadcastService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ReservationWebSocketHandler reservationWebSocketHandler;

    public void broadcastToFrontend(WebUpdateDTO dto) {
        logger.debug("Broadcasting to frontend - Code: {}, Type: {}, Message: {}", 
            dto.getCode(), dto.getType(), dto.getMessage());
        
        messagingTemplate.convertAndSend("/topic/forms", dto);
        logger.debug("STOMP broadcast sent to /topic/forms");
        
        reservationWebSocketHandler.broadcast(dto);
    }

    public void broadcastRawMessage(String message) {
        logger.debug("Broadcasting raw message: {}", message);
        WebUpdateDTO dto = new WebUpdateDTO();
        dto.setMessage(message);
        messagingTemplate.convertAndSend("/topic/forms", dto);
        reservationWebSocketHandler.broadcastMessage(message);
    }
}
