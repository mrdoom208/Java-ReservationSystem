package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.dto.ManageTablesDTO;
import com.mycompany.reservationsystem.model.ManageTables;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TablesService {

    public static List<Map<String, Object>> getAllTables() {
        return ApiClient.getTables();
    }

    public static Map<String, Object> getTableById(Long id) {
        return ApiClient.getTableById(id);
    }

    public static Map<String, Object> createTable(Object table) {
        return ApiClient.createTable(table);
    }

    public static void updateTable(Long id, Object table) {
        ApiClient.updateTable(id, table);
    }

    public static void deleteTable(Long id) {
        ApiClient.deleteTable(id);
    }

    public static long countTables() {
        return ApiClient.countTables();
    }

    public static long countByStatus(String status) {
        return ApiClient.countTablesByStatus(status);
    }

    public static Map<String, Object> getTableStats() {
        return ApiClient.getTableStats();
    }

    public static List<Map<String, Object>> getManageTablesDTOList() {
        return ApiClient.getManageTablesDTOList();
    }

    public static void updateStatus(Long id, String status) {
        Map<String, Object> tableData = Map.of("id", id, "status", status);
        ApiClient.updateTable(id, tableData);
    }

    public static void updateStatusWithStartTime(Long id, String status, LocalTime startTime) {
        Map<String, Object> tableData = new HashMap<>();
        tableData.put("id", id);
        tableData.put("status", status);
        tableData.put("tablestarttime", startTime != null ? startTime.toString() : null);
        ApiClient.updateTable(id, tableData);
    }

    public static void clearStartTime(Long id) {
        try {
            ApiClient.post("/tables/" + id + "/clear-time", "{}");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateStatus(String tableNo, String status) {
        List<Map<String, Object>> tables = ApiClient.getManageTablesDTOList();
        for (Map<String, Object> table : tables) {
            if (tableNo.equals(table.get("tableNo"))) {
                Long id = ((Number) table.get("id")).longValue();
                updateStatus(id, status);
                break;
            }
        }
    }

    public static ManageTables findByNo(String tableNo) {
        List<Map<String, Object>> tables = ApiClient.getManageTablesDTOList();
        for (Map<String, Object> table : tables) {
            if (tableNo.equals(table.get("tableNo"))) {
                ManageTables t = new ManageTables();
                t.setId(((Number) table.get("id")).longValue());
                t.setTableNo((String) table.get("tableNo"));
                t.setCapacity(((Number) table.getOrDefault("capacity", 0)).intValue());
                t.setStatus((String) table.get("status"));
                t.setLocation((String) table.get("location"));
                return t;
            }
        }
        return null;
    }

    public static boolean existsByTableId(Long tableId) {
        List<Map<String, Object>> tables = ApiClient.getManageTablesDTOList();
        for (Map<String, Object> table : tables) {
            if (tableId.equals(table.get("id"))) {
                return true;
            }
        }
        return false;
    }

    public static void deleteById(Long id) {
        ApiClient.deleteTable(id);
    }

    public static void saveTable(ManageTables table) {
        System.out.println("[TablesService] saveTable - id: " + table.getId() + ", status: " + table.getStatus());
        if (table.getId() != null) {
            ApiClient.updateTable(table.getId(), table);
        } else {
            ApiClient.createTable(table);
        }
    }

    public static List<ManageTables> findByStatus(String status) {
        List<ManageTables> tables = new ArrayList<>();
        List<Map<String, Object>> list = ApiClient.getManageTablesDTOList();
        for (Map<String, Object> table : list) {
            if (status.equals(table.get("status"))) {
                ManageTables t = new ManageTables();
                Object idObj = table.get("id");
                if (idObj == null) idObj = table.get("tableId");
                t.setId(idObj != null ? ((Number) idObj).longValue() : 0);
                t.setTableNo((String) table.get("tableNo"));
                t.setCapacity(table.get("capacity") != null ? ((Number) table.get("capacity")).intValue() : 0);
                t.setStatus((String) table.get("status"));
                t.setLocation((String) table.get("location"));
                tables.add(t);
            }
        }
        return tables;
    }

    public static long count() {
        return ApiClient.countTables();
    }

    public static void removeCustomerFromTable(Long tableId) {
        ApiClient.removeCustomerFromTable(tableId);
    }

    public static void clearTableAssignment(String reference) {
        ApiClient.clearReservationTable(reference);
    }

    public static List<ManageTablesDTO> getManageTablesDTO() {
        List<ManageTablesDTO> dtos = new ArrayList<>();
        List<Map<String, Object>> tables = ApiClient.getManageTablesDTOList();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter timeWithMsFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        DateTimeFormatter altTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (Map<String, Object> table : tables) {
            ManageTablesDTO dto = new ManageTablesDTO();
            Object idObj = table.get("id");
            Object tableIdObj = table.get("tableId");
            if (idObj != null) dto.setId(((Number) idObj).longValue());
            if (tableIdObj != null) dto.setTableId(((Number) tableIdObj).longValue());
            dto.setTableNo((String) table.get("tableNo"));
            dto.setCapacity(((Number) table.getOrDefault("capacity", 0)).intValue());
            dto.setStatus((String) table.get("status"));
            dto.setLocation((String) table.get("location"));
            dto.setCustomer((String) table.get("customer"));
            dto.setReference((String) table.get("reference"));
            dto.setPax(table.get("pax") != null ? ((Number) table.get("pax")).intValue() : null);
            
            dto.setTablestarttime(parseTime(table.get("tablestarttime"), timeFormatter, timeWithMsFormatter, altTimeFormatter));
            dto.setTableendtime(parseTime(table.get("tableendtime"), timeFormatter, timeWithMsFormatter, altTimeFormatter));
            dto.setReservationPendingtime(parseTime(table.get("reservationPendingtime"), timeFormatter, timeWithMsFormatter, altTimeFormatter));
            dto.setReservationConfirmtime(parseTime(table.get("reservationConfirmtime"), timeFormatter, timeWithMsFormatter, altTimeFormatter));
            dto.setReservationSeatedtime(parseTime(table.get("reservationSeatedtime"), timeFormatter, timeWithMsFormatter, altTimeFormatter));
            dto.setReservationCompletetime(parseTime(table.get("reservationCompletetime"), timeFormatter, timeWithMsFormatter, altTimeFormatter));
            
            dtos.add(dto);
        }
        return dtos;
    }
    
    private static LocalTime parseTime(Object value, DateTimeFormatter... formatters) {
        if (value == null) return null;
        String timeStr = value.toString();
        if (timeStr.contains(".")) {
            String[] parts = timeStr.split("\\.");
            if (parts.length >= 2) {
                timeStr = parts[0];
            }
        }
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalTime.parse(timeStr, formatter);
            } catch (Exception ignored) {}
        }
        try {
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            return null;
        }
    }
}
