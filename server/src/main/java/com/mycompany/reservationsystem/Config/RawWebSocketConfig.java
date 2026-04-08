package com.mycompany.reservationsystem.config;

import com.mycompany.reservationsystem.websocket.ReservationWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class RawWebSocketConfig implements WebSocketConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(RawWebSocketConfig.class);

    @Autowired
    private ReservationWebSocketHandler reservationWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        logger.info("Registering raw WebSocket endpoint /raw-ws");
        registry.addHandler(reservationWebSocketHandler, "/raw-ws")
                .setAllowedOrigins("*");
    }
}
