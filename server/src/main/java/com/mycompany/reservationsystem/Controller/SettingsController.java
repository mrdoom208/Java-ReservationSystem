package com.mycompany.reservationsystem.rest;

import com.mycompany.reservationsystem.config.AppSettings;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    @PostMapping
    public ResponseEntity<Void> updateSettings(@RequestBody Map<String, Object> settings) {
        if (settings.containsKey("applicationTitle")) {
            AppSettings.saveApplicationTitle((String) settings.get("applicationTitle"));
        }
        if (settings.containsKey("cancelTime")) {
            AppSettings.saveCancelTime((String) settings.get("cancelTime"));
        }
        if (settings.containsKey("resolution")) {
            AppSettings.saveResolution((String) settings.get("resolution"));
        }
        if (settings.containsKey("applicationUrl")) {
            AppSettings.saveApplicationTitle((String) settings.get("applicationUrl"));
        }
        if (settings.containsKey("serialPort")) {
            AppSettings.saveSerialPort((String) settings.get("serialPort"));
        }
        if (settings.containsKey("controller")) {
            AppSettings.saveController((String) settings.get("controller"));
        }
        if (settings.containsKey("module")) {
            AppSettings.saveModule((String) settings.get("module"));
        }
        if (settings.containsKey("phone")) {
            AppSettings.savePhone((String) settings.get("phone"));
        }
        if (settings.containsKey("databaseDeleteTime")) {
            AppSettings.saveDatabaseDeleteTime((String) settings.get("databaseDeleteTime"));
        }
        if (settings.containsKey("messageEnabled")) {
            @SuppressWarnings("unchecked")
            Map<String, Boolean> messageEnabled = (Map<String, Boolean>) settings.get("messageEnabled");
            if (messageEnabled != null) {
                for (Map.Entry<String, Boolean> entry : messageEnabled.entrySet()) {
                    AppSettings.saveMessageEnabled(entry.getKey(), entry.getValue());
                }
            }
        }
        if (settings.containsKey("messageLabel")) {
            @SuppressWarnings("unchecked")
            Map<String, String> messageLabel = (Map<String, String>) settings.get("messageLabel");
            if (messageLabel != null) {
                for (Map.Entry<String, String> entry : messageLabel.entrySet()) {
                    AppSettings.saveMessageLabel(entry.getKey(), entry.getValue());
                }
            }
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/get")
    public Map<String, Object> getSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("applicationTitle", AppSettings.loadApplicationTitle());
        settings.put("cancelTime", AppSettings.loadCancelTime());
        settings.put("cancellationPolicy", AppSettings.loadCancelTime());
        settings.put("resolution", AppSettings.loadResolution());
        settings.put("applicationUrl", AppSettings.loadApplicationUrl());
        settings.put("serialPort", AppSettings.loadSerialPort());
        settings.put("controller", AppSettings.loadController());
        settings.put("module", AppSettings.loadModule());
        settings.put("phone", AppSettings.loadPhone());
        settings.put("databaseDeleteTime", AppSettings.loadDatabaseDeleteTime());
        settings.put("messageEnabled", Map.of(
            "PENDING", AppSettings.loadMessageEnabled("PENDING"),
            "CONFIRMED", AppSettings.loadMessageEnabled("CONFIRMED"),
            "CANCELLED", AppSettings.loadMessageEnabled("CANCELLED"),
            "FINISHED", AppSettings.loadMessageEnabled("FINISHED"),
            "REMINDER", AppSettings.loadMessageEnabled("REMINDER")
        ));
        settings.put("messageLabel", Map.of(
            "PENDING", AppSettings.loadMessageLabel("PENDING"),
            "CONFIRMED", AppSettings.loadMessageLabel("CONFIRMED"),
            "CANCELLED", AppSettings.loadMessageLabel("CANCELLED"),
            "FINISHED", AppSettings.loadMessageLabel("FINISHED"),
            "REMINDER", AppSettings.loadMessageLabel("REMINDER")
        ));
        return settings;
    }

    @PostMapping("/message")
    public ResponseEntity<Void> updateMessageSettings(@RequestBody Map<String, Object> settings) {
        String key = (String) settings.get("key");
        if (settings.containsKey("enabled")) {
            AppSettings.saveMessageEnabled(key, (Boolean) settings.get("enabled"));
        }
        if (settings.containsKey("label")) {
            AppSettings.saveMessageLabel(key, (String) settings.get("label"));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/message/get")
    public Map<String, Object> getMessageSettings(@RequestBody KeyRequest request) {
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", AppSettings.loadMessageEnabled(request.getKey()));
        result.put("label", AppSettings.loadMessageLabel(request.getKey()));
        return result;
    }

    public static class KeyRequest {
        private String key;
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
    }
}
