package com.mycompany.reservationsystem.rest;

import com.mycompany.reservationsystem.dto.ManageTablesDTO;
import com.mycompany.reservationsystem.dto.TableStatsDTO;
import com.mycompany.reservationsystem.dto.WebUpdateDTO;
import com.mycompany.reservationsystem.model.ManageTables;
import com.mycompany.reservationsystem.model.Notification;
import com.mycompany.reservationsystem.repository.ManageTablesRepository;
import com.mycompany.reservationsystem.repository.NotificationRepository;
import com.mycompany.reservationsystem.service.TablesService;
import com.mycompany.reservationsystem.service.WebSocketBroadcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    @Autowired
    private TablesService tablesService;

    @Autowired
    private ManageTablesRepository manageTablesRepository;
    
    @Autowired
    private WebSocketBroadcastService webSocketBroadcastService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private NotificationRepository notificationRepository;

    @PostMapping
    public ManageTables createTable(@RequestBody ManageTables table) {
        return manageTablesRepository.save(table);
    }

    @PostMapping("/all")
    public List<ManageTables> getAllTables() {
        return manageTablesRepository.findAll();
    }

    @PostMapping("/get")
    public ResponseEntity<ManageTables> getTableById(@RequestBody IdRequest request) {
        return manageTablesRepository.findById(request.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ManageTables> updateTable(@PathVariable Long id, @RequestBody ManageTables tableDetails) {
        return manageTablesRepository.findById(id)
                .map(table -> {
                    if (tableDetails.getTableNo() != null) {
                        table.setTableNo(tableDetails.getTableNo());
                    }
                    if (tableDetails.getCapacity() > 0) {
                        table.setCapacity(tableDetails.getCapacity());
                    }
                    if (tableDetails.getStatus() != null) {
                        table.setStatus(tableDetails.getStatus());
                    }
                    if (tableDetails.getLocation() != null) {
                        table.setLocation(tableDetails.getLocation());
                    }
                    if (tableDetails.getTablestarttime() != null) {
                        table.setTablestarttime(tableDetails.getTablestarttime());
                    }
                    if (tableDetails.getTableendtime() != null) {
                        table.setTableendtime(tableDetails.getTableendtime());
                    }
                    if (tableDetails.getReference() != null) {
                        table.setReference(tableDetails.getReference());
                    }
                    ManageTables updated = manageTablesRepository.save(table);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        return manageTablesRepository.findById(id)
                .<ResponseEntity<Void>>map(table -> {
                    if (!"Available".equals(table.getStatus())) {
                        return ResponseEntity.status(403).<Void>build();
                    }
                    tablesService.clearReservationsForTable(id);
                    manageTablesRepository.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/available")
    public List<ManageTables> getAvailableTables() {
        return manageTablesRepository.findByStatus("Available");
    }

    @PostMapping("/stats")
    public TableStatsDTO getTableStats() {
        return tablesService.getTableStats();
    }

    @PostMapping("/dto")
    public List<ManageTablesDTO> getAllTableDTOs() {
        return tablesService.getAllTables();
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<Void> updateTableStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        if (!manageTablesRepository.existsById(id)) {
            return ResponseEntity.notFound().<Void>build();
        }
        manageTablesRepository.findById(id).ifPresent(table -> {
            table.setStatus(request.getStatus());
            manageTablesRepository.save(table);
        });
        return ResponseEntity.ok().<Void>build();
    }

    @PostMapping("/count")
    public long getTableCount() {
        return manageTablesRepository.count();
    }

    @PostMapping("/count/status")
    public long getTableCountByStatus(@RequestBody StatusRequest request) {
        return manageTablesRepository.countByStatus(request.getStatus());
    }

    @PostMapping("/{id}/clear-time")
    public ResponseEntity<ManageTables> clearTableTime(@PathVariable Long id) {
        return manageTablesRepository.findById(id)
                .map(table -> {
                    table.setTablestarttime(null);
                    table.setTableendtime(java.time.LocalTime.now());
                    table.setStatus("Available");
                    table.setReference(null);
                    ManageTables updated = manageTablesRepository.save(table);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/remove-customer")
    public ResponseEntity<ManageTables> removeCustomer(@PathVariable Long id) {
        return manageTablesRepository.findById(id)
                .map(table -> {
                    String reference = table.getReference();
                    tablesService.clearReservationByTableId(id);
                    
                    table.setCustomer(null);
                    table.setCustomerPhone(null);
                    table.setCustomerEmail(null);
                    table.setReference(null);
                    table.setPax(null);
                    table.setTablestarttime(null);
                    table.setTableendtime(java.time.LocalTime.now());
                    table.setStatus("Available");
                    ManageTables updated = manageTablesRepository.save(table);
                    
                    if (reference != null && !reference.isEmpty()) {
                        Notification notification = new Notification();
                        notification.setAccount(reference);
                        notification.setCode("RESERVATION_PENDING");
                        notification.setMessage("Your reservation status has been set to Pending. Please wait for table assignment.");
                        notification.setTimestamp(System.currentTimeMillis());
                        notification.setSent(false);
                        notificationRepository.save(notification);
                        
                        messagingTemplate.convertAndSend("/topic/account." + reference, notification);
                    }
                    
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public static class IdRequest {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    public static class StatusRequest {
        private String status;
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
