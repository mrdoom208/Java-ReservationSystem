package com.mycompany.reservationsystem.config;

import java.util.prefs.Preferences;

public final class AppSettings {
    private static final String EMPTY = "";
    private static final String DEFAULT_APP_TITLE = "Romantic Baboy Reservation System";
    private static final String DEFAULT_APP_TITLE_DISPLAY = "ROMANTIC BABOY RESERVATION SYSTEM";
    private static final String DEFAULT_CANCEL_TIME = "Until Table is Available";
    private static final String DEFAULT_NOSHOW_TIME = "Never";
    private static final String DEFAULT_RESOLUTION = "Fullscreen";
    private static final String DEFAULT_APP_URL = "http://localhost:8080";

    private static final Preferences PREFS =
            Preferences.userRoot().node("reservation-system");

    private AppSettings() {}
    //------------- GENERAL ---------------
    public static void saveApplicationTitle(String title) {
        PREFS.put("ApplicationTitle", defaultIfBlank(title, DEFAULT_APP_TITLE));
    }

    public static String loadApplicationTitle() {
        return PREFS.get("ApplicationTitle", DEFAULT_APP_TITLE_DISPLAY);
    }

    // -------- SERIAL --------
    public static void saveSerialPort(String portName) {
        PREFS.put("serial.port", valueOrEmpty(portName));
    }

    public static String loadSerialPort() {
        return PREFS.get("serial.port", null);
    }

    // -------- DEVICE INFO --------
    public static void saveController(String value) {
        PREFS.put("device.controller", valueOrEmpty(value));
    }
    public static void saveCancelTime(String minute) {
        PREFS.put("cancelTime", valueOrEmpty(minute));
    }

    public static void saveNoshowTime(String minute) {
        PREFS.put("noshowTime", valueOrEmpty(minute));
    }

    public static String loadNoshowTime() {
        return PREFS.get("noshowTime", DEFAULT_NOSHOW_TIME);
    }

    public static int loadNoshowTimeMinutes() {
        String value = loadNoshowTime();
        if (value == null || value.isBlank() || value.equals("Never")) {
            return -1;
        }
        try {
            String numberOnly = value.replaceAll("\\D+", "");
            return Integer.parseInt(numberOnly);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static void saveModule(String value) {
        PREFS.put("device.module", valueOrEmpty(value));
    }

    public static void savePhone(String value) {
        PREFS.put("device.phone", valueOrEmpty(value));
    }

    public static String loadCancelTime() {
        return PREFS.get("cancelTime", DEFAULT_CANCEL_TIME);
    }

    public static int loadCancelTimeMinutes() {
        String value = loadCancelTime(); // e.g., "5 minutes"

        // Extract the first number from the string
        try {
            String numberOnly = value.replaceAll("\\D+", ""); // remove non-digits
            return Integer.parseInt(numberOnly);
        } catch (NumberFormatException e) {
            // fallback to default 5 if parsing fails
            return 5;
        }
    }


    public static String loadController() {
        return PREFS.get("device.controller", "");
    }

    public static String loadModule() {
        return PREFS.get("device.module", "");
    }

    public static String loadPhone() {
        return PREFS.get("device.phone", "");
    }
    //----------------- MESSAGING --------------------------------
    public static void saveMessageLabel(String key, String value) {
        PREFS.put(key, valueOrEmpty(value));
    }

    // Load selected message label for a given reservation type
    public static String loadMessageLabel(String key) {
        return PREFS.get(key, "");
    }

    public static void saveMessagePane(String key, boolean enabled) {
        PREFS.putBoolean(key + ".enabled", enabled);
    }

    // Load toggle state (default to true if missing)
    public static boolean loadMessagePane(String key) {
        return PREFS.getBoolean(key + ".enabled", true);
    }


    public static void saveMessageEnabled(String key, boolean enabled) {
        PREFS.putBoolean(key + ".enabled", enabled);
    }

    // Load toggle state (default to true if missing)
    public static boolean loadMessageEnabled(String key) {
        return PREFS.getBoolean(key + ".enabled", false);
    }


    //--------------- DATABASE INFO -----------------
    public static void saveDatabaseDeleteTime(String value) {
        PREFS.put("database.deleteTime", valueOrEmpty(value));
    }

    public static String loadDatabaseDeleteTime() {
        return PREFS.get("database.deleteTime", "");
    }

    public static void saveResolution(String resolution){
        PREFS.put("resolution", valueOrEmpty(resolution));
    }

    public static String loadResolution(){
        return PREFS.get("resolution", DEFAULT_RESOLUTION);
    }

    public static String loadApplicationUrl() {
        return PREFS.get("app.url", DEFAULT_APP_URL);
    }

    private static String valueOrEmpty(String value) {
        return value == null ? EMPTY : value;
    }

    private static String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
