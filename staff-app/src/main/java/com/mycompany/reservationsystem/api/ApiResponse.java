package com.mycompany.reservationsystem.api;

import java.util.Map;

public class ApiResponse {
    private final boolean success;
    private final Map<String, Object> data;
    private final String error;

    public ApiResponse(boolean success, Map<String, Object> data, String error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public static ApiResponse fromMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return new ApiResponse(false, null, "Empty response");
        }
        if (map.containsKey("error")) {
            return new ApiResponse(false, null, String.valueOf(map.get("error")));
        }
        return new ApiResponse(true, map, null);
    }

    public static ApiResponse success(Map<String, Object> data) {
        return new ApiResponse(true, data, null);
    }

    public static ApiResponse error(String error) {
        return new ApiResponse(false, null, error);
    }
}
