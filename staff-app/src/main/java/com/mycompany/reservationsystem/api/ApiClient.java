package com.mycompany.reservationsystem.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mycompany.reservationsystem.config.AppSettings;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiClient {
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
    
    public static String getBaseUrl() {
        String savedUrl = AppSettings.loadServerUrl();
        if (savedUrl != null && !savedUrl.isEmpty()) {
            if (savedUrl.startsWith("http://") || savedUrl.startsWith("https://")) {
                return savedUrl;
            }
            if (savedUrl.startsWith("ws://") || savedUrl.startsWith("wss://")) {
                return savedUrl.replaceFirst("wss?://", "http://");
            }
        }
        
        String envUrl = System.getenv("SERVER_URL");
        if (envUrl != null && !envUrl.isEmpty()) {
            if (envUrl.startsWith("http://") || envUrl.startsWith("https://")) {
                return envUrl;
            }
            if (envUrl.startsWith("ws://") || envUrl.startsWith("wss://")) {
                return envUrl.replaceFirst("wss?://", "http://");
            }
        }
        return "http://localhost:13472/api";
    }
    
    private static final String BASE_URL;
    private static final ObjectMapper objectMapper = createObjectMapper();
    private static final boolean DEBUG = false;
    private static final CookieManager cookieManager;
    
    static {
        BASE_URL = getBaseUrl();
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL);
        java.net.CookieHandler.setDefault(cookieManager);
    }

    private static String executeRequest(String method, String endpoint, String body) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        
        if (DEBUG) System.out.println("[API] " + method + " " + BASE_URL + endpoint);
        if (DEBUG && body != null) System.out.println("[API] Request: " + body);
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("X-Client-Identifier", AppSettings.loadAppIdentifier());
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setInstanceFollowRedirects(true);
        
        if (body != null && !body.isEmpty()) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }
        
        int responseCode = conn.getResponseCode();
        if (DEBUG) System.out.println("[API] Response Code: " + responseCode);
        
        if (responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpURLConnection.HTTP_OK) {
            if (conn.getInputStream() != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    if (DEBUG) System.out.println("[API] Response: " + response);
                    return response.toString();
                }
            }
            return "";
        }
        
        if (DEBUG) {
            try {
                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                        StringBuilder error = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            error.append(line);
                        }
                        if (DEBUG) System.out.println("[API] Error Response: " + error);
                    }
                }
            } catch (Exception ex) {
                if (DEBUG) System.out.println("[API] Error reading error stream: " + ex.getMessage());
            }
        }
        return "";
    }

    private static String get(String endpoint) throws Exception {
        return executeRequest("GET", endpoint, null);
    }

    public static String post(String endpoint, String body) throws Exception {
        return executeRequest("POST", endpoint, body);
    }

    private static String put(String endpoint, String body) throws Exception {
        return executeRequest("PUT", endpoint, body);
    }

    private static String delete(String endpoint) throws Exception {
        return executeRequest("DELETE", endpoint, null);
    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static List<Map<String, Object>> parseList(String json) {
        try {
            if (json == null || json.isEmpty()) return new ArrayList<>();
            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static Map<String, Object> parseMap(String json) {
        try {
            if (json == null || json.isEmpty()) return Map.of();
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    // User APIs
    public static Map<String, Object> login(String username, String password) {
        try {
            String json = toJson(Map.of("username", username, "password", password));
            String result = post("/auth/login", json);
            return parseMap(result);
        } catch (Exception e) {
            System.err.println("[API] Login Error: " + e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    public static List<Map<String, Object>> getUsers() {
        try {
            return parseList(post("/users/all", "{}"));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static Map<String, Object> getUserById(Long id) {
        try {
            return parseMap(post("/users/get", toJson(Map.of("id", id))));
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    public static Map<String, Object> createUser(Object user) {
        try {
            post("/users", toJson(user));
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    public static void updateUser(Long id, Object user) {
        try {
            put("/users/" + id, toJson(user));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteUser(Long id) {
        try {
            delete("/users/" + id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Reservation APIs
    public static List<Map<String, Object>> getReservations() {
        try {
            return parseList(post("/reservations/all", "{}"));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static Map<String, Object> getPagedReservations(int page, int size) {
        try {
            return parseMap(get("/reservations/paged?page=" + page + "&size=" + size));
        } catch (Exception e) {
            return Map.of("content", new ArrayList<>(), "totalElements", 0);
        }
    }

    public static long getPagedReservationsCount() {
        try {
            String result = get("/reservations/paged/count");
            if (result != null && !result.isEmpty()) {
                return Long.parseLong(result);
            }
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }

    public static Map<String, Object> getReservationByReference(String reference) {
        try {
            return parseMap(post("/reservations/get", toJson(Map.of("reference", reference))));
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    public static Map<String, Object> getReservationById(Long id) {
        try {
            List<Map<String, Object>> all = getReservations();
            for (Map<String, Object> r : all) {
                Object idObj = r.get("id");
                if (idObj != null && ((Number) idObj).longValue() == id) {
                    return r;
                }
            }
            return Map.of("error", "Not found");
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    public static Map<String, Object> createReservation(Object reservation) {
        try {
            post("/reservations", toJson(reservation));
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    public static void updateReservation(Long id, Object reservation) {
        try {
            List<Map<String, Object>> all = getReservations();
            for (Map<String, Object> r : all) {
                Object idObj = r.get("id");
                if (idObj != null && ((Number) idObj).longValue() == id) {
                    String reference = (String) r.get("reference");
                    if (reference != null) {
                        put("/reservations/" + reference, toJson(reservation));
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateReservationByReference(String reference, Object reservation) {
        try {
            String json = toJson(reservation);
            put("/reservations/" + reference, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateReservationStatus(String reference, String status) {
        try {
            String response = put("/reservations/" + reference + "/status?status=" + status, "");
            System.out.println("[ApiClient] updateReservationStatus response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateReservationTable(String reference, Long tableId) {
        try {
            put("/reservations/" + reference + "/table?tableId=" + tableId, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearReservationTable(String reference) {
        try {
            put("/reservations/" + reference + "/table?clear=true", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateReservationSeatedTime(String reference, LocalTime time) {
        try {
            System.out.println(">>> updateReservationSeatedTime: ref=" + reference + ", time=" + time);
            Map<String, Object> request = Map.of("time", time != null ? time.toString() : null);
            put("/reservations/" + reference + "/seated-time", toJson(request));
            System.out.println(">>> PUT request sent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void finishReservation(String reference, String amountPaid, String totalAmount) {
        try {
            StringBuilder params = new StringBuilder();
            if (amountPaid != null) params.append("amountPaid=").append(amountPaid);
            if (totalAmount != null) {
                if (params.length() > 0) params.append("&");
                params.append("totalAmount=").append(totalAmount);
            }
            String endpoint = "/reservations/" + reference + "/finish";
            if (params.length() > 0) endpoint += "?" + params.toString();
            put(endpoint, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteReservation(Long id) {
        try {
            List<Map<String, Object>> all = getReservations();
            for (Map<String, Object> r : all) {
                Object idObj = r.get("id");
                if (idObj != null && ((Number) idObj).longValue() == id) {
                    String reference = (String) r.get("reference");
                    if (reference != null) {
                        delete("/reservations/" + reference);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteReservationByReference(String reference) {
        try {
            delete("/reservations/" + reference);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Customer APIs
    public static List<Map<String, Object>> getCustomers() {
        try {
            return parseList(post("/customers/all", "{}"));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static Map<String, Object> getCustomerByPhone(String phone) {
        try {
            return parseMap(post("/customers/phone", toJson(Map.of("phone", phone))));
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    public static Map<String, Object> getCustomerById(Long id) {
        try {
            return parseMap(post("/customers/get", toJson(Map.of("id", id))));
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    public static Map<String, Object> createCustomer(Object customer) {
        try {
            String result = post("/customers", toJson(customer));
            return parseMap(result);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    // Tables APIs
    public static List<Map<String, Object>> getTables() {
        try {
            return parseList(post("/tables/all", "{}"));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static Map<String, Object> getTableById(Long id) {
        try {
            return parseMap(post("/tables/get", toJson(Map.of("id", id))));
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    public static Map<String, Object> createTable(Object table) {
        try {
            post("/tables", toJson(table));
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    public static void updateTable(Long id, Object table) {
        try {
            String json = toJson(table);
            put("/tables/" + id, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteTable(Long id) {
        try {
            if (DEBUG) System.out.println("[API] DELETE table id: " + id);
            delete("/tables/" + id);
            if (DEBUG) System.out.println("[API] DELETE completed for id: " + id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeCustomerFromTable(Long tableId) {
        try {
            post("/tables/" + tableId + "/remove-customer", "{}");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Message APIs
    public static List<Map<String, Object>> getMessages() {
        try {
            return parseList(post("/messages/all", "{}"));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static Map<String, Object> getMessageById(Long id) {
        try {
            return parseMap(post("/messages/get", toJson(Map.of("id", id))));
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    public static Map<String, Object> createMessage(Object message) {
        try {
            String json = toJson(message);
            return parseMap(post("/messages", json));
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    public static String seedMessages(List<String> labels) {
        try {
            return post("/messages/seed", toJson(labels));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


    public static void updateMessage(Long id, Object message) {
        try {
            put("/messages/" + id, toJson(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Activity Logs APIs
    public static List<Map<String, Object>> getActivityLogs() {
        return getActivityLogs(0, 100);
    }

    public static List<Map<String, Object>> getActivityLogs(int page, int size) {
        try {
            String response = post("/activity-logs/all", toJson(Map.of("page", page, "size", size)));
            Map<String, Object> pageMap = parseMap(response);
            Object content = pageMap.get("content");
            if (content instanceof List) {
                return (List<Map<String, Object>>) content;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static Map<String, Object> getActivityLogById(Long id) {
        try {
            return parseMap(post("/activity-logs/get", toJson(Map.of("id", id))));
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    // Dashboard Stats APIs
    public static Map<String, Object> getDashboardCounts() {
        try {
            return parseMap(post("/reservations/dashboard/counts", "{}"));
        } catch (Exception e) {
            return Map.of();
        }
    }

    public static Map<String, Object> getTableStats() {
        try {
            return parseMap(post("/tables/stats", "{}"));
        } catch (Exception e) {
            return Map.of();
        }
    }

    public static List<Map<String, Object>> getRecentReservations() {
        try {
            return parseList(post("/reservations/recent", "{}"));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // Count methods
    public static long countReservations() {
        try {
            String result = post("/reservations/count", "{}");
            return Long.parseLong(result);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long countReservationsByStatus(String status) {
        try {
            String result = post("/reservations/count/status", toJson(Map.of("status", status)));
            return Long.parseLong(result);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long countTodayReservations() {
        try {
            String today = LocalDate.now().toString();
            List<Map<String, Object>> data = getAllReservationsList();
            return data.stream()
                    .filter(r -> {
                        Object dateObj = r.get("date");
                        if (dateObj instanceof String) {
                            return dateObj.equals(today);
                        }
                        return false;
                    })
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    public static List<Map<String, Object>> getAllReservationsList() {
        return getReservations();
    }

    public static List<Map<String, Object>> getRecentReservationsList() {
        return getRecentReservations();
    }

    public static List<Map<String, Object>> getManageTablesDTOList() {
        try {
            String result = post("/tables/dto", "{}");
            if (DEBUG) System.out.println("[API] /tables/dto response: " + result);
            if (result == null || result.isEmpty() || result.equals("[]")) {
                return new ArrayList<>();
            }
            List<?> rawList = objectMapper.readValue(result, List.class);
            return (List<Map<String, Object>>) (List<?>) rawList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static long countTables() {
        try {
            String result = post("/tables/count", "{}");
            return Long.parseLong(result);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long countTablesByStatus(String status) {
        try {
            String result = post("/tables/count/status", toJson(Map.of("status", status)));
            return Long.parseLong(result);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long countCustomers() {
        try {
            String result = post("/customers/count", "{}");
            return Long.parseLong(result);
        } catch (Exception e) {
            return 0;
        }
    }

    // Reports APIs
    public static List<Map<String, Object>> getSalesReports(String startDate, String endDate) {
        try {
            return parseList(post("/reports/sales", toJson(Map.of("startDate", startDate, "endDate", endDate))));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static List<Map<String, Object>> getTableUsageReports(String startDate, String endDate) {
        try {
            return parseList(post("/reports/table-usage", toJson(Map.of("startDate", startDate, "endDate", endDate))));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static List<Map<String, Object>> getCustomerReports() {
        try {
            return parseList(post("/reports/customers", "{}"));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static List<Map<String, Object>> getReservationTableLogs() {
        try {
            return parseList(post("/reservations/table-logs", "{}"));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    public static List<Map<String, Object>> getReservationTableLogsData(String from, String to) {
        try {
            Map<String, Object> request = new HashMap<>();
            if (from != null && !from.isEmpty()) request.put("from", from);
            if (to != null && !to.isEmpty()) request.put("to", to);
            return parseList(post("/reservations/table-logs", toJson(request)));
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Unread Messages
    public static long getUnreadMessagesCount() {
        try {
            String result = post("/messages/unread/count", "{}");
            return Long.parseLong(result);
        } catch (Exception e) {
            return 0;
        }
    }

    public static void markMessageAsRead(Long id) {
        try {
            put("/messages/" + id + "/read", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Object> sendSms(Object smsRequest) {
        try {
            post("/messages/send", toJson(smsRequest));
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    // Permission APIs
    public static List<Map<String, Object>> getPermissions() {
        try {
            return parseList(post("/permissions/all", "{}"));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    public static Map<String, Object> getPermissionSnapshot() {
        try {
            return parseMap(post("/permissions/snapshot", "{}"));
        } catch (Exception e) {
            return Map.of();
        }
    }

    public static Map<String, Object> getPermissionById(Long id) {
        try {
            return parseMap(post("/permissions/get", toJson(Map.of("id", id))));
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    public static void updatePermission(Long id, Object permission) {
        try {
            put("/permissions/" + id, toJson(permission));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Settings APIs
    public static Map<String, Object> getSettings() {
        try {
            return parseMap(post("/settings/get", "{}"));
        } catch (Exception e) {
            return Map.of();
        }
    }

    public static Map<String, Object> updateSettings(Object settings) {
        try {
            post("/settings", toJson(settings));
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    // Customer Report APIs
    public static List<Map<String, Object>> getReservationCustomerDTO(String phone, String from, String to) {
        try {
            return parseList(post("/reservations/customer-detail", toJson(Map.of("phone", phone, "from", from != null ? from : "", "to", to != null ? to : ""))));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static Map<String, Object> getPagedCustomerReport(String from, String to, int page, int size) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("page", page);
            request.put("size", size);
            if (from != null && !from.isEmpty()) request.put("from", from);
            if (to != null && !to.isEmpty()) request.put("to", to);
            return parseMap(post("/reservations/customer-report/paged", toJson(request)));
        } catch (Exception e) {
            return Map.of("content", new ArrayList<>(), "totalElements", 0);
        }
    }

    public static List<Map<String, Object>> getSalesReportsData(String from, String to) {
        try {
            return parseList(post("/reservations/sales-reports", toJson(Map.of("from", from, "to", to))));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static List<Map<String, Object>> getTableUsageReportsData(String from, String to) {
        try {
            return parseList(post("/reservations/table-usage-reports", toJson(Map.of("from", from, "to", to))));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}