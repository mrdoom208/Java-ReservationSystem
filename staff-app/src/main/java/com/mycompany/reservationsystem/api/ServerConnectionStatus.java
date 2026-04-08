package com.mycompany.reservationsystem.api;

import com.mycompany.reservationsystem.config.AppSettings;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ServerConnectionStatus {
    
    public enum Status {
        CONNECTED,
        DISCONNECTED,
        CHECKING
    }
    
    private static final AtomicReference<Status> currentStatus = new AtomicReference<>(Status.CHECKING);
    private static final AtomicBoolean isChecking = new AtomicBoolean(false);
    
    public static Status getStatus() {
        return currentStatus.get();
    }
    
    public static void setStatus(Status status) {
        currentStatus.set(status);
    }
    
    public static boolean isConnected() {
        return currentStatus.get() == Status.CONNECTED;
    }
    
    private static String getServerUrl() {
        String saved = AppSettings.loadServerUrl();
        if (saved != null && !saved.isEmpty()) {
            return saved;
        }
        return System.getenv().getOrDefault("SERVER_URL", "http://localhost:8080/api");
    }
    
    public static boolean checkConnection() {
        if (isChecking.compareAndSet(false, true)) {
            currentStatus.set(Status.CHECKING);
            
            Thread thread = new Thread(() -> {
                try {
                    String baseUrl = getServerUrl();
                    URL url = new URL(baseUrl + "/health");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                        currentStatus.set(Status.CONNECTED);
                    } else {
                        currentStatus.set(Status.DISCONNECTED);
                    }
                } catch (Exception e) {
                    currentStatus.set(Status.DISCONNECTED);
                } finally {
                    isChecking.set(false);
                }
            });
            thread.start();
            
            return true;
        }
        return false;
    }
    
    public static String getStatusMessage() {
        switch (currentStatus.get()) {
            case CONNECTED:
                return "Connected to server";
            case DISCONNECTED:
                return "Disconnected from server";
            case CHECKING:
                return "Checking connection...";
            default:
                return "Unknown";
        }
    }
}
