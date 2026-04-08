package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.dto.CustomerReportDTO;
import com.mycompany.reservationsystem.dto.ReservationCustomerDTO;
import com.mycompany.reservationsystem.dto.SalesReportsDTO;
import com.mycompany.reservationsystem.model.Customer;
import com.mycompany.reservationsystem.model.ManageTables;
import com.mycompany.reservationsystem.model.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReservationService {

    public static List<Map<String, Object>> getAllReservations() {
        return ApiClient.getReservations();
    }

    public static Map<String, Object> getReservationById(Long id) {
        return ApiClient.getReservationById(id);
    }

    public static Map<String, Object> createReservation(Object reservation) {
        return ApiClient.createReservation(reservation);
    }

    public static void updateReservation(Long id, Object reservation) {
        ApiClient.updateReservation(id, reservation);
    }

    public static void deleteReservation(Long id) {
        ApiClient.deleteReservation(id);
    }

    public static long countReservations() {
        return ApiClient.countReservations();
    }

    public static long countByStatus(String status) {
        return ApiClient.countReservationsByStatus(status);
    }

    public static long countTodayCustomers() {
        return ApiClient.countTodayReservations();
    }

    public static List<Map<String, Object>> getRecentReservations() {
        return ApiClient.getRecentReservations();
    }

    public static List<Map<String, Object>> getTableLogs() {
        return ApiClient.getReservationTableLogs();
    }

    public static List<Map<String, Object>> getAllReservationsList() {
        return ApiClient.getAllReservationsList();
    }

    public static List<Map<String, Object>> getRecentReservationsList() {
        return ApiClient.getRecentReservationsList();
    }

    public static List<Map<String, Object>> getReservationCustomerDTO(String phone, String from, String to) {
        return ApiClient.getReservationCustomerDTO(phone, from, to);
    }

    public static List<Map<String, Object>> getSalesReportsData(String from, String to) {
        return ApiClient.getSalesReportsData(from, to);
    }

    public static List<Map<String, Object>> getTableUsageReportsData(String from, String to) {
        return ApiClient.getTableUsageReportsData(from, to);
    }

    public static List<SalesReportsDTO> getSalesReportsPage(LocalDate from, LocalDate to, int page, int size) {
        List<SalesReportsDTO> allData = getSalesReports(from, to);
        int start = page * size;
        int end = Math.min(start + size, allData.size());
        if (start >= allData.size()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(allData.subList(start, end));
    }

    public static long getTotalReservationCount() {
        return ApiClient.getPagedReservationsCount();
    }

    @SuppressWarnings("unchecked")
    public static List<Reservation> loadPage(int page, int size) {
        List<Reservation> reservations = new ArrayList<>();
        Map<String, Object> response = ApiClient.getPagedReservations(page, size);
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.getOrDefault("content", new ArrayList<>());
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter altTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (Map<String, Object> map : list) {
            Reservation r = new Reservation();
            r.setId(((Number) map.getOrDefault("id", 0)).longValue());
            r.setReference((String) map.get("reference"));
            r.setStatus((String) map.get("status"));
            Object paxObj = map.get("pax");
            if (paxObj instanceof Number) r.setPax(((Number) paxObj).intValue());
            Object dateObj = map.get("date");
            if (dateObj instanceof String) {
                r.setDate(LocalDate.parse((String) dateObj));
            }
            r.setReservationPendingtime(parseTime(map.get("reservationPendingtime"), timeFormatter, altTimeFormatter));
            r.setReservationNotifiedtime(parseTime(map.get("reservationNotifiedtime"), timeFormatter, altTimeFormatter));
            r.setReservationConfirmtime(parseTime(map.get("reservationConfirmtime"), timeFormatter, altTimeFormatter));
            r.setReservationCancelledtime(parseTime(map.get("reservationCancelledtime"), timeFormatter, altTimeFormatter));
            r.setReservationSeatedtime(parseTime(map.get("reservationSeatedtime"), timeFormatter, altTimeFormatter));
            r.setReservationCompletetime(parseTime(map.get("reservationCompletetime"), timeFormatter, altTimeFormatter));
            r.setReservationNoshowtime(parseTime(map.get("reservationNoshowtime"), timeFormatter, altTimeFormatter));
            Object customerObj = map.get("customer");
            if (customerObj instanceof Map) {
                Map<String, Object> customerMap = (Map<String, Object>) customerObj;
                Customer c = new Customer();
                c.setId(customerMap.get("id") != null ? ((Number) customerMap.get("id")).longValue() : null);
                c.setName((String) customerMap.get("name"));
                c.setPhone((String) customerMap.get("phone"));
                c.setEmail((String) customerMap.get("email"));
                r.setCustomer(c);
            }
            Object tableObj = map.get("table");
            if (tableObj instanceof Map) {
                Map<String, Object> tableMap = (Map<String, Object>) tableObj;
                ManageTables t = new ManageTables();
                t.setId(tableMap.get("id") != null ? ((Number) tableMap.get("id")).longValue() : null);
                t.setTableNo((String) tableMap.get("tableNo"));
                t.setStatus((String) tableMap.get("status"));
                t.setCapacity(tableMap.get("capacity") != null ? ((Number) tableMap.get("capacity")).intValue() : 0);
                r.setTable(t);
            }
            reservations.add(r);
        }
        return reservations;
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

    @SuppressWarnings("unchecked")
    public static List<Reservation> loadByDate(LocalDate from, LocalDate to) {
        List<Reservation> reservations = new ArrayList<>();
        List<Map<String, Object>> list = ApiClient.getAllReservationsList();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter altTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (Map<String, Object> map : list) {
            Object dateObj = map.get("date");
            if (dateObj instanceof String) {
                LocalDate date = LocalDate.parse((String) dateObj);
                if (!date.isBefore(from) && !date.isAfter(to)) {
                    Reservation r = new Reservation();
                    r.setId(((Number) map.getOrDefault("id", 0)).longValue());
                    r.setReference((String) map.get("reference"));
                    r.setStatus((String) map.get("status"));
                    Object paxObj = map.get("pax");
                    if (paxObj instanceof Number) r.setPax(((Number) paxObj).intValue());
                    r.setDate(LocalDate.parse((String) dateObj));
                    r.setReservationPendingtime(parseTime(map.get("reservationPendingtime"), timeFormatter, altTimeFormatter));
                    r.setReservationNotifiedtime(parseTime(map.get("reservationNotifiedtime"), timeFormatter, altTimeFormatter));
                    r.setReservationConfirmtime(parseTime(map.get("reservationConfirmtime"), timeFormatter, altTimeFormatter));
                    r.setReservationCancelledtime(parseTime(map.get("reservationCancelledtime"), timeFormatter, altTimeFormatter));
                    r.setReservationSeatedtime(parseTime(map.get("reservationSeatedtime"), timeFormatter, altTimeFormatter));
                    r.setReservationCompletetime(parseTime(map.get("reservationCompletetime"), timeFormatter, altTimeFormatter));
                    r.setReservationNoshowtime(parseTime(map.get("reservationNoshowtime"), timeFormatter, altTimeFormatter));
                    Object customerObj = map.get("customer");
                    if (customerObj instanceof Map) {
                        Map<String, Object> customerMap = (Map<String, Object>) customerObj;
                        Customer c = new Customer();
                        c.setId(customerMap.get("id") != null ? ((Number) customerMap.get("id")).longValue() : null);
                        c.setName((String) customerMap.get("name"));
                        c.setPhone((String) customerMap.get("phone"));
                        c.setEmail((String) customerMap.get("email"));
                        r.setCustomer(c);
                    }
                    Object tableObj = map.get("table");
                    if (tableObj instanceof Map) {
                        Map<String, Object> tableMap = (Map<String, Object>) tableObj;
                        ManageTables t = new ManageTables();
                        t.setId(tableMap.get("id") != null ? ((Number) tableMap.get("id")).longValue() : null);
                        t.setTableNo((String) tableMap.get("tableNo"));
                        t.setStatus((String) tableMap.get("status"));
                        t.setCapacity(tableMap.get("capacity") != null ? ((Number) tableMap.get("capacity")).intValue() : 0);
                        r.setTable(t);
                    }
                    reservations.add(r);
                }
            }
        }
        return reservations;
    }

    public static void updateSeatedtime(String reference, LocalTime time) {
        ApiClient.updateReservationSeatedTime(reference, time);
    }

    public static void updateCompletetime(String reference, LocalTime time) {
        Map<String, Object> updateData = Map.of(
            "reservationCompletetime", time != null ? time.toString() : null
        );
        ApiClient.updateReservationByReference(reference, updateData);
    }

    public static void updateStatus(String reference, String status) {
        ApiClient.updateReservationStatus(reference, status);
    }

    public static Reservation findById(Long id) {
        List<Map<String, Object>> list = ApiClient.getAllReservationsList();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter altTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (Map<String, Object> map : list) {
            Object idObj = map.get("id");
            if (idObj != null && idObj instanceof Number) {
                if (((Number) idObj).longValue() == id) {
                    Reservation r = new Reservation();
                    r.setId(((Number) map.getOrDefault("id", 0)).longValue());
                    r.setReference((String) map.get("reference"));
                    r.setStatus((String) map.get("status"));
                    Object paxObj = map.get("pax");
                    if (paxObj instanceof Number) r.setPax(((Number) paxObj).intValue());
                    Object dateObj = map.get("date");
                    if (dateObj instanceof String) r.setDate(LocalDate.parse((String) dateObj));
                    r.setReservationPendingtime(parseTime(map.get("reservationPendingtime"), timeFormatter, altTimeFormatter));
                    r.setReservationNotifiedtime(parseTime(map.get("reservationNotifiedtime"), timeFormatter, altTimeFormatter));
                    r.setReservationConfirmtime(parseTime(map.get("reservationConfirmtime"), timeFormatter, altTimeFormatter));
                    r.setReservationCancelledtime(parseTime(map.get("reservationCancelledtime"), timeFormatter, altTimeFormatter));
                    r.setReservationSeatedtime(parseTime(map.get("reservationSeatedtime"), timeFormatter, altTimeFormatter));
                    r.setReservationCompletetime(parseTime(map.get("reservationCompletetime"), timeFormatter, altTimeFormatter));
                    r.setReservationNoshowtime(parseTime(map.get("reservationNoshowtime"), timeFormatter, altTimeFormatter));
                    Object customerObj = map.get("customer");
                    if (customerObj instanceof Map) {
                        Map<String, Object> customerMap = (Map<String, Object>) customerObj;
                        Customer c = new Customer();
                        c.setId(customerMap.get("id") != null ? ((Number) customerMap.get("id")).longValue() : null);
                        c.setName((String) customerMap.get("name"));
                        c.setPhone((String) customerMap.get("phone"));
                        c.setEmail((String) customerMap.get("email"));
                        r.setCustomer(c);
                    }
                    Object tableObj = map.get("table");
                    if (tableObj instanceof Map) {
                        Map<String, Object> tableMap = (Map<String, Object>) tableObj;
                        ManageTables table = new ManageTables();
                        table.setId(tableMap.get("id") != null ? ((Number) tableMap.get("id")).longValue() : null);
                        table.setTableNo((String) tableMap.get("tableNo"));
                        table.setStatus((String) tableMap.get("status"));
                        table.setCapacity(tableMap.get("capacity") != null ? ((Number) tableMap.get("capacity")).intValue() : 0);
                        r.setTable(table);
                    }
                    return r;
                }
            }
        }
        return null;
    }

    public static void saveReservation(Reservation reservation) {
        if (reservation.getReference() != null && !reservation.getReference().isEmpty()) {
            ApiClient.updateReservationByReference(reservation.getReference(), reservation);
        } else if (reservation.getId() != null) {
            ApiClient.updateReservation(reservation.getId(), reservation);
        } else {
            ApiClient.createReservation(reservation);
        }
    }

    public static List<ReservationCustomerDTO> getReservationCustomerDTOByPhoneAndDate(String phone, LocalDate from, LocalDate to) {
        List<ReservationCustomerDTO> dtos = new ArrayList<>();
        List<Map<String, Object>> data = ApiClient.getReservationCustomerDTO(
                phone,
                from != null ? from.toString() : "",
                to != null ? to.toString() : ""
        );
        for (Map<String, Object> map : data) {
            ReservationCustomerDTO dto = new ReservationCustomerDTO();
            Object idObj = map.get("reservationId");
            if (idObj instanceof Number) {
                dto.setId(((Number) idObj).longValue());
            }
            Object nameObj = map.get("customerName");
            if (nameObj instanceof String) dto.setCustomerName((String) nameObj);
            Object phoneObj = map.get("customerPhone");
            if (phoneObj instanceof String) dto.setPhoneNumber((String) phoneObj);
            Object dateObj = map.get("date");
            if (dateObj instanceof String) dto.setLastVisit((String) dateObj);
            Object revObj = map.get("sales");
            if (revObj instanceof Number) dto.setTotalSpent(((Number) revObj).doubleValue());
            Object paxObj = map.get("pax");
            if (paxObj instanceof Number) dto.setTotalVisits(((Number) paxObj).intValue());
            Object statusObj = map.get("status");
            if (statusObj instanceof String) dto.setStatus((String) statusObj);
            dtos.add(dto);
        }
        return dtos;
    }

    public static List<SalesReportsDTO> getSalesReports(LocalDate from, LocalDate to) {
        List<SalesReportsDTO> reports = new ArrayList<>();
        List<Map<String, Object>> data = ApiClient.getSalesReportsData(
                from != null ? from.toString() : "",
                to != null ? to.toString() : ""
        );
        for (Map<String, Object> map : data) {
            SalesReportsDTO dto = new SalesReportsDTO();
            Object dateObj = map.get("date");
            if (dateObj instanceof String) {
                dto.setDate(LocalDate.parse((String) dateObj));
            }
            Object revObj = map.get("totalSales");
            if (revObj instanceof Number) {
                dto.setSales(((Number) revObj).doubleValue());
            }
            Object resObj = map.get("totalReservation");
            if (resObj instanceof Number) {
                dto.setReservationCount(((Number) resObj).intValue());
            }
            Object custObj = map.get("totalCustomer");
            if (custObj instanceof Number) {
                dto.setTotalCustomer(((Number) custObj).longValue());
            }
            reports.add(dto);
        }
        return reports;
    }

    public static boolean existsByTableId(Long tableId) {
        List<Map<String, Object>> list = ApiClient.getAllReservationsList();
        for (Map<String, Object> map : list) {
            String status = (String) map.get("status");
            if ("Pending".equals(status) || "Confirmed".equals(status) || "Seated".equals(status)) {
                Object tableObj = map.get("table");
                if (tableObj instanceof Map) {
                    Map<String, Object> tableMap = (Map<String, Object>) tableObj;
                    if (tableId.equals(tableMap.get("id"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static List<CustomerReportDTO> loadPageAsCustomerReport(int page, int size) {
        Map<String, Object> response = ApiClient.getPagedCustomerReport(null, null, page, size);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.getOrDefault("content", new ArrayList<>());
        List<CustomerReportDTO> reports = new ArrayList<>();
        for (Map<String, Object> map : content) {
            CustomerReportDTO dto = new CustomerReportDTO();
            dto.setPhone(map.get("phone") != null ? map.get("phone").toString() : "");
            Object totalObj = map.get("totalReservation");
            if (totalObj instanceof Number) dto.setTotalVisits(((Number) totalObj).intValue());
            Object spentObj = map.get("totalSales");
            if (spentObj instanceof Number) dto.setTotalSpent(((Number) spentObj).doubleValue());
            Object avgObj = map.get("averageSales");
            if (avgObj instanceof Number) dto.setAverageSpend(((Number) avgObj).doubleValue());
            reports.add(dto);
        }
        return reports;
    }

    public static List<CustomerReportDTO> loadByDateAsCustomerReport(LocalDate from, LocalDate to) {
        Map<String, CustomerReportDTO> aggregated = new LinkedHashMap<>();
        List<Map<String, Object>> list = ApiClient.getAllReservationsList();
        for (Map<String, Object> map : list) {
            Object dateObj = map.get("date");
            if (!(dateObj instanceof String)) continue;
            LocalDate date = LocalDate.parse((String) dateObj);
            if (date.isBefore(from) || date.isAfter(to)) continue;

            Object customerObj = map.get("customer");
            if (!(customerObj instanceof Map)) continue;
            Map<String, Object> customerMap = (Map<String, Object>) customerObj;
            String phone = (String) customerMap.get("phone");
            if (phone == null || phone.isBlank()) continue;

            CustomerReportDTO dto = aggregated.computeIfAbsent(phone, k -> {
                CustomerReportDTO d = new CustomerReportDTO();
                d.setPhone(phone);
                d.setCustomerName((String) customerMap.get("name"));
                d.setTotalVisits(0);
                d.setTotalSpent(0);
                return d;
            });

            dto.setTotalVisits(dto.getTotalVisits() + 1);
            Object revObj = map.get("sales");
            if (revObj instanceof Number) {
                dto.setTotalSpent(dto.getTotalSpent() + ((Number) revObj).doubleValue());
            }
            String lastVisit = dto.getLastVisit();
            String dateStr = (String) dateObj;
            if (lastVisit == null || dateStr.compareTo(lastVisit) > 0) {
                dto.setLastVisit(dateStr);
            }
        }
        return new ArrayList<>(aggregated.values());
    }
}
