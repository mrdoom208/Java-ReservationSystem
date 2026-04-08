package com.mycompany.reservationsystem.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.reservationsystem.dto.WebUpdateDTO;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ReservationWebSocketServer extends WebSocketServer {

    private static final Logger logger = LoggerFactory.getLogger(ReservationWebSocketServer.class);
    
    private final Set<WebSocket> clients = Collections.synchronizedSet(new HashSet<>());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static ReservationWebSocketServer instance;
    private int port;

    public ReservationWebSocketServer(int port) {
        super(new InetSocketAddress(port));
        this.port = port;
        instance = this;
        logger.info("Raw WebSocket server configured for port {}", port);
    }

    public static ReservationWebSocketServer getInstance() {
        return instance;
    }

    public static void setInstance(ReservationWebSocketServer instance) {
        ReservationWebSocketServer.instance = instance;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clients.add(conn);
        logger.info("Frontend connected: {}", conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        clients.remove(conn);
        logger.info("Frontend disconnected: {}, code: {}, reason: {}", conn.getRemoteSocketAddress(), code, reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.info("Received from {}: {}", conn.getRemoteSocketAddress(), message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.error("WebSocket error for {}: {}", conn != null ? conn.getRemoteSocketAddress() : "unknown", ex.getMessage());
    }

    @Override
    public void onStart() {
        logger.info("Raw WebSocket server started on port {}", port);
    }

    public void broadcast(WebUpdateDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            synchronized (clients) {
                for (WebSocket client : clients) {
                    if (client.isOpen()) {
                        client.send(json);
                    }
                }
            }
            logger.debug("Broadcast to {} clients: {}", clients.size(), json);
        } catch (Exception e) {
            logger.error("Failed to broadcast: {}", e.getMessage());
        }
    }

    public int getConnectedCount() {
        return clients.size();
    }
}