package com.mycompany.reservationsystem.websocket;

import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.util.NotificationManager;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class ReservationWebSocketClient extends org.java_websocket.client.WebSocketClient {
    private static String getWsUrl() {
        String saved = AppSettings.loadWebsocketUrl();
        if (saved != null && !saved.isEmpty()) {
            return saved;
        }
        return System.getenv().getOrDefault("WEBSOCKET_URL", "ws://localhost:8080/raw-ws");
    }
    
    private static ReservationWebSocketClient instance;

    private ReservationWebSocketClient() throws URISyntaxException {
        super(new URI(getWsUrl()));
    }

    public static ReservationWebSocketClient getInstance() {
        if (instance == null || !instance.getConnection().isOpen()) {
            try {
                instance = new ReservationWebSocketClient();
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return null;
            }
        }
        return instance;
    }

    public static void connectToServer() {
        try {
            if (instance != null && instance.getConnection().isOpen()) {
                return;
            }
            instance = new ReservationWebSocketClient();
            instance.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("WebSocket Connected");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received: " + message);
        
        if (message.contains("\"type\":\"reservation\"")) {
            NotificationManager.show("New Reservation", "A new reservation has been made!", NotificationManager.NotificationType.SUCCESS);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket Closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("WebSocket Error: " + ex.getMessage());
    }
}
