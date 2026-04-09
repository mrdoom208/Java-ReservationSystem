package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.api.ApiClient;
import java.util.Map;

public class WebsiteSyncService {

    public static void syncTables() {
        ApiClient.getTables();
    }

    public static void syncReservations() {
        ApiClient.getReservations();
    }

    public static void sendAutoCancelTime(int minutes) {
        try {
            String url = ApiClient.getBaseUrl() + "/api/settings";
            String cancelValue;
            if (minutes < 0) {
                cancelValue = "Never";
            } else {
                cancelValue = minutes + " minutes";
            }
            String payload = "{\"cancelTime\":\"" + cancelValue + "\"}";
            ApiClient.post(url, payload);
            System.out.println("Synced auto-cancel time to server: " + cancelValue);
        } catch (Exception e) {
            System.err.println("Failed to sync auto-cancel time: " + e.getMessage());
        }
    }

    public static void sendAutoDeleteMonths(int months) {
        try {
            String url = ApiClient.getBaseUrl() + "/api/settings";
            String payload = "{\"databaseDeleteTime\":\"" + months + " months\"}";
            ApiClient.post(url, payload);
            System.out.println("Synced auto-delete time to server: " + months + " months");
        } catch (Exception e) {
            System.err.println("Failed to sync auto-delete time: " + e.getMessage());
        }
    }

    public static void syncMessageSettings(String key, boolean enabled, String label) {
        try {
            String url = ApiClient.getBaseUrl() + "/api/settings/message";
            String payload = String.format("{\"key\":\"%s\",\"enabled\":%b,\"label\":\"%s\"}", key, enabled, label != null ? label : "");
            ApiClient.post(url, payload);
            System.out.println("Synced message setting to server: " + key);
        } catch (Exception e) {
            System.err.println("Failed to sync message setting: " + e.getMessage());
        }
    }
}
