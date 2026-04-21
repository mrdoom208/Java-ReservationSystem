package com.mycompany.reservationsystem.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class AppSettings {
    private static final String SETTINGS_FILE = "settings.properties";
    private static final String LOG_FILE = "app.log";
    private static Properties properties = new Properties();
    private static Path settingsPath;

    static {
        initPaths();
        loadSettings();
    }

    private static void initPaths() {
        Path appDir = Paths.get(System.getProperty("user.home"), "ReservationSystem");
        try {
            if (!Files.exists(appDir)) {
                Files.createDirectories(appDir);
            }
            settingsPath = appDir.resolve(SETTINGS_FILE);
        } catch (IOException e) {
            settingsPath = Paths.get(SETTINGS_FILE);
        }
    }

    private static void log(String message) {
        try (PrintWriter pw = new PrintWriter(new java.io.FileWriter(LOG_FILE, true))) {
            pw.println(message);
        } catch (IOException e) {
            System.err.println("[AppSettings] " + message);
        }
    }

    private static void loadSettings() {
        try {
            Path workingDirSettings = Paths.get(System.getProperty("user.dir"), SETTINGS_FILE);
            if (Files.exists(workingDirSettings)) {
                try (InputStream input = new FileInputStream(workingDirSettings.toFile())) {
                    properties.load(input);
                    log("Loaded settings from working dir: " + workingDirSettings.toAbsolutePath());
                    return;
                } catch (IOException ex) {
                    log("Error loading from working dir: " + ex.getMessage());
                }
            }
            
            if (Files.exists(settingsPath)) {
                try (InputStream input = new FileInputStream(settingsPath.toFile())) {
                    properties.load(input);
                    log("Loaded settings from: " + settingsPath.toAbsolutePath());
                }
            } else {
                Path workingDir = Paths.get("settings.properties");
                if (Files.exists(workingDir)) {
                    try (InputStream input = new FileInputStream(workingDir.toFile())) {
                        properties.load(input);
                        log("Loaded settings from working dir: " + workingDir.toAbsolutePath());
                    }
                } else {
                    log("Settings file not found, using defaults. Path: " + settingsPath.toAbsolutePath());
                }
            }
        } catch (IOException ex) {
            log("Could not load settings: " + ex.getMessage());
        }
    }

    private static void saveSettings() {
        try (FileOutputStream output = new FileOutputStream(settingsPath.toFile())) {
            properties.store(output, "Application Settings");
            log("Saved settings to: " + settingsPath.toAbsolutePath());
        } catch (IOException ex) {
            log("ERROR saving settings: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static String loadApplicationTitle() {
        return properties.getProperty("app.title", "Reservation System");
    }

    public static void saveApplicationTitle(String title) {
        properties.setProperty("app.title", title);
        saveSettings();
    }

    public static String loadResolution() {
        return properties.getProperty("app.resolution", "1280x720");
    }

    public static void saveResolution(String resolution) {
        properties.setProperty("app.resolution", resolution);
        saveSettings();
    }

    public static String loadCancelTime() {
        return properties.getProperty("app.cancelTime", "30");
    }

    public static void saveCancelTime(String cancelTime) {
        properties.setProperty("app.cancelTime", cancelTime);
        saveSettings();
    }

    public static String loadNoshowTime() {
        return properties.getProperty("app.noshowTime", "Never");
    }

    public static void saveNoshowTime(String noshowTime) {
        properties.setProperty("app.noshowTime", noshowTime);
        saveSettings();
    }

    public static boolean loadAutoCancelEnabled() {
        return Boolean.parseBoolean(properties.getProperty("app.autoCancelEnabled", "true"));
    }

    public static void saveAutoCancelEnabled(boolean enabled) {
        properties.setProperty("app.autoCancelEnabled", String.valueOf(enabled));
        saveSettings();
    }

    public static boolean loadAutoNoshowEnabled() {
        return Boolean.parseBoolean(properties.getProperty("app.autoNoshowEnabled", "true"));
    }

    public static void saveAutoNoshowEnabled(boolean enabled) {
        properties.setProperty("app.autoNoshowEnabled", String.valueOf(enabled));
        saveSettings();
    }

    public static String loadSerialPort() {
        return properties.getProperty("serial.port", "");
    }

    public static void saveSerialPort(String port) {
        properties.setProperty("serial.port", port);
        saveSettings();
    }

    public static String loadController() {
        return properties.getProperty("sms.controller", "");
    }

    public static void saveController(String controller) {
        properties.setProperty("sms.controller", controller);
        saveSettings();
    }

    public static String loadModule() {
        return properties.getProperty("sms.module", "");
    }

    public static void saveModule(String module) {
        properties.setProperty("sms.module", module);
        saveSettings();
    }

    public static String loadPhone() {
        return properties.getProperty("sms.phone", "");
    }

    public static void savePhone(String phone) {
        properties.setProperty("sms.phone", phone);
        saveSettings();
    }

    public static boolean loadMessagePane(String key) {
        return Boolean.parseBoolean(properties.getProperty("message.pane." + key, "false"));
    }

    public static void saveMessagePane(String key, boolean value) {
        properties.setProperty("message.pane." + key, String.valueOf(value));
        saveSettings();
    }

    public static boolean loadMessageEnabled(String key) {
        return Boolean.parseBoolean(properties.getProperty("message.enabled." + key, "true"));
    }

    public static void saveMessageEnabled(String key, boolean value) {
        properties.setProperty("message.enabled." + key, String.valueOf(value));
        saveSettings();
    }

    public static String loadMessageLabel(String key) {
        return properties.getProperty("message.label." + key, "");
    }

    public static void saveMessageLabel(String key, String label) {
        properties.setProperty("message.label." + key, label);
        saveSettings();
    }

    public static String loadDatabaseDeleteTime() {
        return properties.getProperty("database.deleteTime", "12");
    }

    public static void saveDatabaseDeleteTime(String months) {
        properties.setProperty("database.deleteTime", months);
        saveSettings();
    }

    public static String loadPhilSmsSenderId() {
        return properties.getProperty("philsms.sender.id", "");
    }

    public static void savePhilSmsSenderId(String id) {
        properties.setProperty("philsms.sender.id", id);
        saveSettings();
    }

    public static String loadPhilSmsApiToken() {
        return properties.getProperty("philsms.api.token", "");
    }

    public static void savePhilSmsApiToken(String token) {
        properties.setProperty("philsms.api.token", token);
        saveSettings();
    }

    public static String loadServerUrl() {
        String val = properties.getProperty("server.url", "");
        return val.isEmpty() ? null : val;
    }

    public static void saveServerUrl(String url) {
        if (url != null && url.endsWith("/api")) {
            url = url.substring(0, url.length() - 4);
        }
        if (url != null && url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        properties.setProperty("server.url", url);
        saveSettings();
    }

    public static String loadWebsiteUrl() {
        return properties.getProperty("website.url", "");
    }

    public static void saveWebsiteUrl(String url) {
        properties.setProperty("website.url", url);
        saveSettings();
    }

    public static String loadWebsocketUrl() {
        String val = properties.getProperty("websocket.url", "");
        return val.isEmpty() ? null : val;
    }

    public static void saveWebsocketUrl(String url) {
        properties.setProperty("websocket.url", url);
        saveSettings();
    }

    public static String loadAppIdentifier() {
        return properties.getProperty("app.identifier", "staff-app-default");
    }

    public static void saveAppIdentifier(String identifier) {
        properties.setProperty("app.identifier", identifier);
        saveSettings();
    }
}
