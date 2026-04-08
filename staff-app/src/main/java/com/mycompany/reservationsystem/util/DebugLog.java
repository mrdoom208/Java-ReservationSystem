package com.mycompany.reservationsystem.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DebugLog {

    private static final String LOG_FILE = "debug.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static void log(String source, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry = String.format("[%s] [%s] %s", timestamp, source, message);
        
        System.out.println(logEntry);
        
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(logEntry);
        } catch (IOException e) {
            System.err.println("Failed to write to debug log: " + e.getMessage());
        }
    }

    public static void logError(String source, String message, Throwable throwable) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry = String.format("[%s] [ERROR] [%s] %s", timestamp, source, message);
        String stackTrace = throwable != null ? getStackTrace(throwable) : "";
        
        System.err.println(logEntry);
        if (!stackTrace.isEmpty()) {
            System.err.println(stackTrace);
        }
        
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(logEntry);
            if (!stackTrace.isEmpty()) {
                pw.println(stackTrace);
            }
        } catch (IOException e) {
            System.err.println("Failed to write to debug log: " + e.getMessage());
        }
    }

    public static void logWebSocket(String action, String details) {
        log("WEBSOCKET", action + ": " + details);
    }

    public static void logNetwork(String action, String details) {
        log("NETWORK", action + ": " + details);
    }

    public static void clearLog() {
        try (FileWriter fw = new FileWriter(LOG_FILE, false)) {
        } catch (IOException e) {
            System.err.println("Failed to clear debug log: " + e.getMessage());
        }
    }

    private static String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("    at ").append(element).append("\n");
        }
        return sb.toString();
    }
}