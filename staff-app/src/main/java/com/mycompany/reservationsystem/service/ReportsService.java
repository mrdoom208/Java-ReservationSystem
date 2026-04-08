package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.dto.TableUsageInformationDTO;
import com.mycompany.reservationsystem.dto.TableUsageReportDTO;
import com.mycompany.reservationsystem.dto.ReservationTablelogsDTO;
import com.mycompany.reservationsystem.model.ReservationTableLogs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReportsService {

    public static List<Map<String, Object>> getSalesReports(String startDate, String endDate) {
        return ApiClient.getSalesReports(startDate, endDate);
    }

    public static List<Map<String, Object>> getTableUsageReports(String startDate, String endDate) {
        return ApiClient.getTableUsageReports(startDate, endDate);
    }

    public static List<Map<String, Object>> getCustomerReports() {
        return ApiClient.getCustomerReports();
    }

    public static List<ReservationTableLogs> findByDateBetween(LocalDate from, LocalDate to) {
        List<Map<String, Object>> data;
        if (from != null && to != null) {
            data = ApiClient.getReservationTableLogsData(from.toString(), to.toString());
        } else {
            data = ApiClient.getReservationTableLogsData("", "");
        }
        
        List<ReservationTableLogs> logs = new ArrayList<>();
        if (data == null || data.isEmpty()) {
            return logs;
        }
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter altTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (Map<String, Object> map : data) {
            ReservationTableLogs log = new ReservationTableLogs();
            log.setCustomer((String) map.get("customer"));
            Object paxObj = map.get("pax");
            if (paxObj instanceof Number) log.setPax(((Number) paxObj).intValue());
            log.setPhone((String) map.get("phone"));
            log.setEmail((String) map.get("email"));
            log.setReference((String) map.get("reference"));
            log.setStatus((String) map.get("status"));
            log.setTableNo((String) map.get("tableNo"));
            Object capObj = map.get("tablecapacity");
            if (capObj instanceof Number) log.setTablecapacity(((Number) capObj).intValue());
            log.setTablelocation((String) map.get("tablelocation"));
            
            log.setReservationPendingtime(parseTime(map.get("reservationPendingtime"), timeFormatter, altTimeFormatter));
            log.setReservationConfirmtime(parseTime(map.get("reservationConfirmtime"), timeFormatter, altTimeFormatter));
            log.setReservationSeatedtime(parseTime(map.get("reservationSeatedtime"), timeFormatter, altTimeFormatter));
            log.setReservationCompletetime(parseTime(map.get("reservationCompletetime"), timeFormatter, altTimeFormatter));
            log.setTablestarttime(parseTime(map.get("tablestarttime"), timeFormatter, altTimeFormatter));
            log.setTableendtime(parseTime(map.get("tableendtime"), timeFormatter, altTimeFormatter));
            log.setOccupiedTime(parseTime(map.get("reservationSeatedtime"), timeFormatter, altTimeFormatter));
            
            Object dateObj = map.get("date");
            if (dateObj instanceof String) log.setDate(LocalDate.parse((String) dateObj));
            
            Object revObj = map.get("Sales");
            if (revObj instanceof Number) log.setSales(new BigDecimal(revObj.toString()));
            
            log.setPrefer((String) map.get("prefer"));
            Object tableIdObj = map.get("tableid");
            if (tableIdObj instanceof Number) log.setTableid(((Number) tableIdObj).longValue());
            log.setTimestamp((String) map.get("timestamp"));
            log.setAction((String) map.get("status"));
            
            logs.add(log);
        }
        return logs;
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

    public static List<TableUsageReportDTO> getTableUsageReport(LocalDate from, LocalDate to) {
        List<TableUsageReportDTO> reports = new ArrayList<>();
        List<Map<String, Object>> data;
        if (from != null && to != null) {
            data = ApiClient.getTableUsageReportsData(from.toString(), to.toString());
        } else {
            data = ApiClient.getTableUsageReportsData("", "");
        }
        if (data == null || data.isEmpty()) {
            return reports;
        }
        for (Map<String, Object> map : data) {
            TableUsageReportDTO dto = new TableUsageReportDTO();
            Object tableNoObj = map.get("tableNo");
            if (tableNoObj == null) tableNoObj = map.get("tableNumber");
            dto.setTableNo(tableNoObj != null ? tableNoObj.toString() : "unknown");
            
            Object resObj = map.get("totalReservation");
            if (resObj instanceof Number) {
                dto.setTotalReservations(((Number) resObj).intValue());
            }
            Object custObj = map.get("totalCustomer");
            if (custObj instanceof Number) {
                dto.setTotalCustomer(((Number) custObj).intValue());
            }
            Object revObj = map.get("totalSales");
            if (revObj instanceof Number) {
                dto.setTotalSales(((Number) revObj).doubleValue());
            }
            reports.add(dto);
        }
        return reports;
    }
    
    public static List<TableUsageReportDTO> getTableUsageReportPage(LocalDate from, LocalDate to, int page, int size) {
        List<TableUsageReportDTO> allData = getTableUsageReport(from, to);
        int start = page * size;
        int end = Math.min(start + size, allData.size());
        if (start >= allData.size()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(allData.subList(start, end));
    }

    public static List<TableUsageInformationDTO> getTableUsageInfo(LocalDate from, LocalDate to, String tableNo) {
        List<TableUsageInformationDTO> info = new ArrayList<>();
        List<Map<String, Object>> allData = ApiClient.getTableUsageReportsData(
                from != null ? from.toString() : "",
                to != null ? to.toString() : ""
        );
        for (Map<String, Object> map : allData) {
            Object tno = map.get("tableNo");
            if (tno == null) tno = map.get("tableNumber");
            if (tno == null) tno = map.get("tablenumber");
            if (tableNo != null && tableNo.equals(tno != null ? tno.toString() : "")) {
                TableUsageInformationDTO dto = new TableUsageInformationDTO();
                Object tableIdObj = map.get("tableId");
                if (tableIdObj instanceof Number) dto.setTableId(((Number) tableIdObj).longValue());
                dto.setTableNumber(tno != null ? tno.toString() : "");
                dto.setTableNo(tno != null ? tno.toString() : "");
                Object refObj = map.get("reference");
                if (refObj == null) refObj = map.get("reservationReference");
                dto.setReference(refObj != null ? refObj.toString() : "");
                Object paxObj = map.get("pax");
                if (paxObj instanceof Number) dto.setPax(((Number) paxObj).intValue());
                Object revObj = map.get("sales");
                if (revObj instanceof Number) dto.setSales(((Number) revObj).doubleValue());
                Object timeObj = map.get("time");
                if (timeObj == null) timeObj = map.get("reservationPendingtime");
                dto.setTime(timeObj != null ? timeObj.toString() : "");
                Object dateObj = map.get("date");
                if (dateObj instanceof String) dto.setDate((String) dateObj);
                Object resObj = map.get("totalReservation");
                if (resObj instanceof Number) dto.setReservationCount(((Number) resObj).intValue());
                info.add(dto);
            }
        }
        return info;
    }

    public static void saveLog(ReservationTableLogs log) {
        try {
            String json = ApiClient.toJson(log);
            ApiClient.post("/reservations/table-logs/save", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
