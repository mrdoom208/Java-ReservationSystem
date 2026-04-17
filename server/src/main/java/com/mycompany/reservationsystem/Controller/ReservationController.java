package com.mycompany.reservationsystem.rest;

import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.dto.*;
import com.mycompany.reservationsystem.model.ManageTables;
import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.model.ReservationTableLogs;
import com.mycompany.reservationsystem.model.Customer;
import com.mycompany.reservationsystem.repository.CustomerRepository;
import com.mycompany.reservationsystem.repository.ManageTablesRepository;
import com.mycompany.reservationsystem.repository.MessageRepository;
import com.mycompany.reservationsystem.repository.ReservationRepository;
import com.mycompany.reservationsystem.repository.ReservationTableLogsRepository;
import com.mycompany.reservationsystem.service.ReservationService;
import com.mycompany.reservationsystem.service.SmsService;
import com.mycompany.reservationsystem.service.WebSocketBroadcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private WebSocketBroadcastService webSocketBroadcastService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ManageTablesRepository manageTablesRepository;

    @Autowired
    private ReservationTableLogsRepository tableLogsRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SmsService smsService;

    @PostMapping
    public Reservation createReservation(@RequestBody ReservationRequest request) {
        Reservation reservation = new Reservation();
        reservation.setPax(request.getPax());
        reservation.setPrefer(request.getPrefer());
        if (request.getReference() == null || request.getReference().isBlank()) {
            reservation.setReference(generateReference());
        } else {
            reservation.setReference(request.getReference());
        }
        reservation.setDate(request.getDate());
        reservation.setReservationPendingtime(request.getReservationPendingtime());
        if (request.getTableId() != null) {
            manageTablesRepository.findById(request.getTableId()).ifPresent(table -> {
                reservation.setTable(table);
                reservation.setStatus("Confirm");
                reservation.setReservationConfirmtime(java.time.LocalTime.now());
            });
        } else {
            reservation.setStatus(request.getStatus() != null ? request.getStatus() : "Pending");
        }
        if (request.getCustomerId() != null) {
            customerRepository.findById(request.getCustomerId()).ifPresent(reservation::setCustomer);
        }
        Reservation saved = reservationRepository.save(reservation);
        
        WebUpdateDTO dto = new WebUpdateDTO();
        dto.setCode("reservation");
        dto.setMessage("New reservation created: " + saved.getReference());
        dto.setType("reservation");
        try {
            webSocketBroadcastService.broadcastToFrontend(dto);
        } catch (Exception e) {
            // Log but don't fail reservation creation
        }
        
        sendNewReservationSms(saved);
        sendAlwaysOnReservationSms(saved);
        
        return saved;
    }

    private void sendNewReservationSms(Reservation reservation) {
        try {
            boolean apiEnabled = AppSettings.loadMessageEnabled("api.toggle");
            if (!apiEnabled) {
                return;
            }
            
            String messageEnabledKey = "message.enabled.message.new";
            boolean isEnabled = AppSettings.loadMessageEnabled(messageEnabledKey);
            if (!isEnabled) {
                return;
            }
            
            String messageLabelKey = "message.label.message.new";
            String messageLabel = AppSettings.loadMessageLabel(messageLabelKey);
            
            String messageText = null;
            if (messageLabel != null && !messageLabel.isEmpty()) {
                messageText = loadMessageText(messageLabel);
            }
            
            if (messageText == null || messageText.isEmpty()) {
                messageText = buildDefaultNewReservationMessage(reservation);
            }
            
            if (reservation.getCustomer() != null && reservation.getCustomer().getPhone() != null) {
                smsService.sendSms(reservation.getCustomer().getPhone(), messageText);
            }
        } catch (Exception e) {
            // Silent fail for SMS
        }
    }

    private String loadMessageText(String label) {
        return messageRepository.findByMessageLabelIgnoreCase(label)
                .map(m -> m.getMessageDetails())
                .orElse(null);
    }

    private String buildDefaultNewReservationMessage(Reservation reservation) {
        String name = reservation.getCustomer() != null ? reservation.getCustomer().getName() : "";
        String ref = reservation.getReference() != null ? reservation.getReference() : "";
        return "Hello " + name + "! Your reservation (Ref: " + ref + ") has been confirmed for " 
                + reservation.getDate() + ". We look forward to serving you!";
    }

    private void sendAlwaysOnReservationSms(Reservation reservation) {
        try {
            if (reservation.getCustomer() == null || reservation.getCustomer().getPhone() == null) {
                return;
            }
            String name = reservation.getCustomer().getName() != null ? reservation.getCustomer().getName() : "Guest";
            String ref = reservation.getReference() != null ? reservation.getReference() : "";
            String pax = reservation.getPax() > 0 ? String.valueOf(reservation.getPax()) : "0";
            String message = String.format(
                "Hello %s, your reservation has been successfully made.\nReference: %s\nParty Size: %s\nWe are looking forward to welcoming you!",
                name, ref, pax
            );
            smsService.sendSms(reservation.getCustomer().getPhone(), message);
        } catch (Exception e) {
            // Silent fail for SMS
        }
    }

    @PostMapping("/all")
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @GetMapping("/paged")
    public Page<Reservation> getPagedReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return reservationRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date", "reservationPendingtime")));
    }

    @GetMapping("/paged/count")
    public long getPagedReservationsCount() {
        return reservationRepository.count();
    }

    @PostMapping("/get")
    public ResponseEntity<Reservation> getReservationByReference(@RequestBody ReferenceRequest request) {
        return reservationRepository.findByReference(request.getReference())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{reference}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable String reference, @RequestParam String status) {
        reservationService.updateStatus(reference, status);
        
        WebUpdateDTO dto = new WebUpdateDTO();
        dto.setCode("status");
        dto.setMessage("Reservation " + reference + " status updated to " + status);
        dto.setType("update");
        try {
            webSocketBroadcastService.broadcastToFrontend(dto);
        } catch (Exception e) {
            // Log but don't fail status update
        }
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reference}/table")
    public ResponseEntity<Void> updateTable(@PathVariable String reference, @RequestBody UpdateTableRequest request) {
        ManageTables table = manageTablesRepository.findById(request.getTableId()).orElse(null);
        if (table == null) {
            return ResponseEntity.notFound().<Void>build();
        }
        reservationService.updateTableId(reference, table);
        
        WebUpdateDTO dto = new WebUpdateDTO();
        dto.setCode("table");
        dto.setMessage("Reservation " + reference + " table updated");
        dto.setType("update");
        try {
            webSocketBroadcastService.broadcastToFrontend(dto);
        } catch (Exception e) {
            // Log but don't fail table update
        }
        
        return ResponseEntity.ok().<Void>build();
    }

    @PutMapping("/{reference}/table")
    public ResponseEntity<Void> updateTableByReference(@PathVariable String reference,
            @RequestParam(value = "tableId", required = false) Long tableId,
            @RequestParam(value = "clear", required = false) Boolean clear) {
        
        if (clear != null && clear) {
            reservationService.clearTableFromReservation(reference);
            WebUpdateDTO dto = new WebUpdateDTO();
            dto.setCode("table");
            dto.setMessage("Reservation " + reference + " table cleared");
            dto.setType("update");
            try {
                webSocketBroadcastService.broadcastToFrontend(dto);
            } catch (Exception e) {
                // Log but don't fail table clear
            }
            return ResponseEntity.ok().<Void>build();
        }
        
        if (tableId != null) {
            ManageTables table = manageTablesRepository.findById(tableId).orElse(null);
            if (table == null) {
                return ResponseEntity.notFound().<Void>build();
            }
            reservationService.updateTableId(reference, table);
            
            WebUpdateDTO dto = new WebUpdateDTO();
            dto.setCode("table");
            dto.setMessage("Reservation " + reference + " table updated");
            dto.setType("update");
            try {
                webSocketBroadcastService.broadcastToFrontend(dto);
            } catch (Exception e) {
                // Log but don't fail table update
            }
        }
        
        return ResponseEntity.ok().<Void>build();
    }

    @PutMapping("/{reference}/seated-time")
    public ResponseEntity<Void> updateSeatedTime(@PathVariable String reference, @RequestBody UpdateSeatedTimeRequest request) {

        reservationService.updateSeatedtime(reference, request.getTime());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reference}/finish")
    public ResponseEntity<Void> finishReservation(@PathVariable String reference, @RequestBody FinishReservationRequest request) {
        reservationService.updateStatus(reference, "Complete");
        reservationService.clearTableOnly(reference);
        if (request.getAmountPaid() != null) {
            reservationService.updateAmountPaid(reference, request.getAmountPaid());
        }
        if (request.getTotalAmount() != null) {
            reservationService.updateTotalAmount(reference, request.getTotalAmount());
        }
        reservationService.updateFinishedTime(reference, LocalTime.now());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reference}/delete")
    public ResponseEntity<Void> deleteReservation(@PathVariable String reference) {
        reservationService.deleteByReference(reference);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{reference}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable String reference, @RequestBody Reservation reservationDetails) {
        return reservationRepository.findByReference(reference)
                .map(reservation -> {
                    if (reservationDetails.getPax() > 0) {
                        reservation.setPax(reservationDetails.getPax());
                    }
                    if (reservationDetails.getPrefer() != null) {
                        reservation.setPrefer(reservationDetails.getPrefer());
                    }
                    if (reservationDetails.getDate() != null) {
                        reservation.setDate(reservationDetails.getDate());
                    }
                    if (reservationDetails.getReservationPendingtime() != null) {
                        reservation.setReservationPendingtime(reservationDetails.getReservationPendingtime());
                    }
                    if (reservationDetails.getStatus() != null) {
                        reservation.setStatus(reservationDetails.getStatus());
                    }
                    if (reservationDetails.getReservationCompletetime() != null) {
                        reservation.setReservationCompletetime(reservationDetails.getReservationCompletetime());
                    }
                    if (reservationDetails.getSales() != null) {
                        reservation.setSales(reservationDetails.getSales());
                    }
                    if (reservationDetails.getTable() != null) {
                        reservation.setTable(reservationDetails.getTable());
                    }
                    
                    if (reservationDetails.getCustomer() != null) {
                        if (reservationDetails.getCustomer().getName() != null) {
                            reservation.getCustomer().setName(reservationDetails.getCustomer().getName());
                        }
                        if (reservationDetails.getCustomer().getPhone() != null) {
                            reservation.getCustomer().setPhone(reservationDetails.getCustomer().getPhone());
                        }
                        if (reservationDetails.getCustomer().getEmail() != null) {
                            reservation.getCustomer().setEmail(reservationDetails.getCustomer().getEmail());
                        }
                        customerRepository.save(reservation.getCustomer());
                    }
                    
                    Reservation updated = reservationRepository.save(reservation);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/by-date")
    public List<Reservation> getReservationsByDate(@RequestBody DateRequest request) {
        return reservationRepository.findByDate(request.getDate());
    }

    @PostMapping("/by-status")
    public List<Reservation> getReservationsByStatus(@RequestBody StatusRequest request) {
        return reservationRepository.findByStatus(request.getStatus());
    }

    @PostMapping("/active")
    public List<Reservation> getActiveReservations() {
        return reservationRepository.findActiveReservations();
    }

    @PostMapping("/customer-report")
    public List<CustomerReportDTO> getCustomerReport(@RequestBody CustomerReportRequest request) {
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String normalizedPhone = request.getPhone().trim().replace(" ", "+");
            return reservationRepository.getFilteredCustomerReport(request.getFrom(), request.getTo()).stream()
                    .filter(dto -> normalizedPhone.equals(dto.getPhone()))
                    .toList();
        }
        return reservationRepository.getFilteredCustomerReport(request.getFrom(), request.getTo());
    }

    @PostMapping("/customer-report/paged")
    public Map<String, Object> getPagedCustomerReport(@RequestBody CustomerReportPagedRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 100;
        Page<CustomerReportDTO> result = reservationRepository.getFilteredCustomerReportPaged(
                request.getFrom(), request.getTo(), PageRequest.of(page, size));
        return Map.of(
            "content", result.getContent(),
            "totalElements", result.getTotalElements(),
            "totalPages", result.getTotalPages(),
            "currentPage", result.getNumber()
        );
    }

    @PostMapping("/customer-detail")
    public List<ReservationCustomerDTO> getCustomerDetailReport(@RequestBody CustomerDetailRequest request) {
        return reservationRepository.getReservationCustomerDTOByPhoneAndDate(
                request.getPhone(), request.getFrom(), request.getTo());
    }

    @PostMapping("/table-usage-report")
    public List<TableUsageReportDTO> getTableUsageReport(@RequestBody TableUsageReportRequest request) {
        if (request.getDate() != null) {
            return reservationRepository.getTableUsageReport(request.getDate());
        }
        return reservationRepository.getAllTableUsageReport();
    }

    @PostMapping("/dashboard/today")
    public Map<String, Object> getTodayDashboard() {
        return reservationService.getDashboardCounts();
    }

    @PostMapping("/dashboard/counts")
    public Map<String, Object> getDashboardCounts() {
        return reservationService.getDashboardCounts();
    }

    @PostMapping("/count")
    public long getReservationCount() {
        return reservationRepository.count();
    }

    @PostMapping("/count/status")
    public long getReservationCountByStatus(@RequestBody StatusRequest request) {
        return reservationRepository.countByStatus(request.getStatus());
    }

    @PostMapping("/recent")
    public List<Reservation> getRecentReservations() {
        return reservationRepository.findTop15ByOrderByDateDescReservationPendingtimeDesc(PageRequest.of(0, 15));
    }

    @PostMapping("/sales-reports")
    public List<SalesReportsDTO> getSalesReports(@RequestBody DateRangeRequest request) {
        return reservationRepository.getSalesReports(request.getFrom(), request.getTo());
    }

    @PostMapping("/table-usage-reports")
    public List<TableUsageReportDTO> getTableUsageReports(@RequestBody DateRangeRequest request) {
        if (request.getFrom() != null && request.getTo() != null) {
            return reservationRepository.getTableUsageReportBetween(request.getFrom(), request.getTo());
        }
        return reservationRepository.getAllTableUsageReport();
    }

    @PostMapping("/table-logs")
    public List<ReservationTableLogs> getTableLogs(@RequestBody DateRangeRequest request) {
        if (request.getFrom() != null && request.getTo() != null) {
            return tableLogsRepository.findByDateBetween(request.getFrom(), request.getTo());
        }
        return tableLogsRepository.findAll();
    }

    @PostMapping("/table-logs/save")
    public ResponseEntity<ReservationTableLogs> saveTableLog(@RequestBody ReservationTableLogs log) {
        ReservationTableLogs saved = tableLogsRepository.save(log);
        return ResponseEntity.ok(saved);
    }

    private String generateReference() {
        return "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
