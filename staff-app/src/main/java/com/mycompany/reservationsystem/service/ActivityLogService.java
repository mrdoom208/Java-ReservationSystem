package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.api.ApiClient;

public class ActivityLogService {

    public static void getAllActivityLogs() {
        ApiClient.getActivityLogs();
    }

    public static void logAction(String user, String position, String module, String action, String description) {
        System.out.println("Activity Log: " + action + " - " + module + " - " + description);
        try {
            ApiClient.post("/activity-logs", ApiClient.toJson(new java.util.LinkedHashMap<String, Object>() {{
                put("user", user);
                put("position", position);
                put("module", module);
                put("action", action);
                put("description", description);
            }}));
        } catch (Exception e) {
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }
}
