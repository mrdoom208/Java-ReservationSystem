package com.mycompany.reservationsystem.websocket;

import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.util.NotificationManager;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
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
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(null, null, null);
            setSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        System.out.println("WebSocket Closed: " + reason + " (code: " + code + ", remote: " + remote + ")");
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("WebSocket Error: " + ex.getMessage());
        try {
            String userDir = System.getProperty("user.home");
            java.io.File debugFile = new java.io.File(userDir, "websocket_debug.txt");
            java.io.FileWriter fw = new java.io.FileWriter(debugFile, false);
            java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);
            bw.write("=== WEBSOCKET ERROR ===");
            bw.newLine();
            bw.write("Time: " + java.time.LocalDateTime.now());
            bw.newLine();
            bw.write("URL: " + getURI());
            bw.newLine();
            bw.write("Exception: " + ex.getClass().getName());
            bw.newLine();
            bw.write("Message: " + ex.getMessage());
            bw.newLine();
            bw.write("Stack Trace:");
            bw.newLine();
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            ex.printStackTrace(pw);
            bw.write(sw.toString());
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            System.err.println("Debug write error: " + e.getMessage());
        }
    }
}
