package com.mycompany.reservationsystem.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.dto.WebUpdateDTO;
import com.mycompany.reservationsystem.util.DebugLog;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient {
    private static String getWsUrl() {
        String saved = AppSettings.loadWebsocketUrl();
        if (saved != null && !saved.isEmpty()) {
            return saved;
        }
        return System.getenv().getOrDefault("WEBSOCKET_URL", "ws://localhost:13473/raw-ws");
    }
    
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static WebSocketClient instance;
    private final List<WebSocketListener> listeners = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
        .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
    private boolean isReconnecting = false;
    private boolean isManualDisconnect = false;
    private int reconnectAttempts = 0;

    private WebSocketClient() throws URISyntaxException {
        super(new URI(getWsUrl()));
        configureSSL();
        DebugLog.logWebSocket("INIT", "WebSocketClient created with URL: " + getWsUrl());
    }

    public WebSocketClient(String domain) throws URISyntaxException {
        super(new URI(domain));
        configureSSL();
        DebugLog.logWebSocket("INIT", "WebSocketClient created with custom URL: " + domain);
    }

    private void configureSSL() {
        try {
            String[] tlsVersions = {"TLSv1.3", "TLSv1.2"};
            SSLContext sslContext = null;
            for (String tls : tlsVersions) {
                try {
                    sslContext = SSLContext.getInstance(tls);
                    sslContext.init(null, null, null);
                    DebugLog.logWebSocket("SSL", "TLS configured: " + tls);
                    break;
                } catch (Exception e) {
                    DebugLog.logWebSocket("SSL", "Failed to configure " + tls + ": " + e.getMessage());
                }
            }
            if (sslContext == null) {
                throw new Exception("Failed to create SSL context");
            }
            setSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            DebugLog.logError("WEBSOCKET", "Failed to configure SSL", e);
        }
    }

    public static WebSocketClient getInstance() {
        if (instance == null) {
            try {
                instance = new WebSocketClient();
            } catch (URISyntaxException e) {
                DebugLog.logError("WEBSOCKET", "Failed to create WebSocketClient instance", e);
                return null;
            }
        }
        return instance;
    }

    public void addListener(WebSocketListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            DebugLog.logWebSocket("LISTENER", "Added listener, total: " + listeners.size());
        }
    }

    public void removeListener(WebSocketListener listener) {
        listeners.remove(listener);
        DebugLog.logWebSocket("LISTENER", "Removed listener, total: " + listeners.size());
    }

    public void connect() {
        URI uri = getURI();
        DebugLog.logWebSocket("CONNECT", "URL: " + uri);
        DebugLog.logWebSocket("CONNECT", "Host: " + (uri != null ? uri.getHost() : "null") + ":" + (uri != null ? uri.getPort() : "null"));
        DebugLog.logWebSocket("CONNECT", "Scheme: " + (uri != null ? uri.getScheme() : "null"));
        isManualDisconnect = false;
        reconnectAttempts = 0;
        isReconnecting = false;
        if (reconnectExecutor.isShutdown()) {
            reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        super.connect();
    }

    public void disconnect() {
        isManualDisconnect = true;
        isReconnecting = false;
        if (!reconnectExecutor.isShutdown()) {
            reconnectExecutor.shutdownNow();
        }
        DebugLog.logWebSocket("DISCONNECT", "Closing connection");
        super.close();
    }

    private void startReconnect() {
        if (isReconnecting || isManualDisconnect) {
            return;
        }
        
        if (reconnectExecutor.isShutdown()) {
            reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        
        isReconnecting = true;
        reconnectExecutor.scheduleAtFixedRate(() -> {
            if (!isOpen() && !isManualDisconnect) {
                if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                    isReconnecting = false;
                    DebugLog.logWebSocket("RECONNECT", "Max reconnect attempts reached. Stopping.");
                    for (WebSocketListener listener : listeners) {
                        listener.onConnectionError("Max reconnect attempts reached");
                    }
                    return;
                }
                
                reconnectAttempts++;
                DebugLog.logWebSocket("RECONNECT", "Attempting to reconnect... (" + reconnectAttempts + "/" + MAX_RECONNECT_ATTEMPTS + ")");
                for (WebSocketListener listener : listeners) {
                    listener.onConnecting();
                }
                try {
                    super.connect();
                } catch (Exception e) {
                    DebugLog.logError("WEBSOCKET", "Reconnect failed", e);
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void stopReconnect() {
        isReconnecting = false;
        reconnectAttempts = 0;
    }
    
    public void resetReconnectAttempts() {
        reconnectAttempts = 0;
        isReconnecting = false;
    }
    
    public void forceReconnect() {
        reconnectAttempts = 0;
        isReconnecting = false;
        isManualDisconnect = false;
        if (reconnectExecutor.isShutdown()) {
            reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        DebugLog.logWebSocket("CONNECT", "Forcing reconnect to: " + getURI());
        super.connect();
    }

    public void sendDTO(WebUpdateDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            DebugLog.logWebSocket("SEND", "Sending DTO: " + json);
            send(json);
        } catch (Exception e) {
            DebugLog.logError("WEBSOCKET", "Failed to send DTO", e);
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        String msg = "WebSocket Connected to: " + getURI();
        System.out.println(msg);
        DebugLog.logWebSocket("OPEN", msg);
        stopReconnect();
        for (WebSocketListener listener : listeners) {
            listener.onConnected();
        }
    }

    @Override
    public void onMessage(String message) {
        DebugLog.logWebSocket("RECEIVE", "Message received: " + message);
        System.out.println("Received: " + message);
        
        try {
            WebUpdateDTO dto = objectMapper.readValue(message, WebUpdateDTO.class);
            
            for (WebSocketListener listener : listeners) {
                try {
                    listener.onMessage(dto);
                } catch (Exception e) {
                    DebugLog.logError("WEBSOCKET", "Error processing message", e);
                }
            }
        } catch (Exception e) {
            DebugLog.logError("WEBSOCKET", "Failed to parse message JSON", e);
            
            WebUpdateDTO dto = new WebUpdateDTO();
            dto.setMessage(message);
            
            for (WebSocketListener listener : listeners) {
                try {
                    listener.onMessage(dto);
                } catch (Exception ex) {
                    DebugLog.logError("WEBSOCKET", "Error processing message", ex);
                }
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        String msg = "WebSocket Closed - Code: " + code + ", Reason: " + reason + ", Remote: " + remote;
        System.out.println(msg);
        DebugLog.logWebSocket("CLOSE", msg);
        
        if (!isManualDisconnect) {
            if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                DebugLog.logWebSocket("CLOSE", "Max reconnect attempts already reached, not restarting");
                return;
            }
            for (WebSocketListener listener : listeners) {
                listener.onConnecting();
            }
            startReconnect();
        }
    }

    @Override
    public void onError(Exception ex) {
        if (ex == null || ex.getMessage() == null) {
            return;
        }
        String msg = "WebSocket Error: " + ex.getMessage();
        System.out.println(msg);
        DebugLog.logError("WEBSOCKET", msg, ex);
        
        String cause = ex.getCause() != null ? ex.getCause().getMessage() : "no cause";
        DebugLog.logWebSocket("ERROR", "Cause: " + cause);
        
        if (ex.getMessage().contains("handshake")) {
            DebugLog.logWebSocket("ERROR", "SSL handshake failed - check TLS compatibility");
            DebugLog.logWebSocket("ERROR", "URL was: " + getURI());
        }
    }
}
