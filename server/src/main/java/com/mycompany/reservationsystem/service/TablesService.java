package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.dto.ManageTablesDTO;
import com.mycompany.reservationsystem.dto.TableStatsDTO;
import com.mycompany.reservationsystem.model.ManageTables;
import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.repository.ManageTablesRepository;
import com.mycompany.reservationsystem.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class TablesService {

    @Autowired
    private ManageTablesRepository manageTablesRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    public void updateStatus(Long id, String newStatus) {
        Optional<ManageTables> optional = manageTablesRepository.findById(id);
        if (optional.isPresent()) {
            ManageTables entity = optional.get();
            entity.setStatus(newStatus);
            manageTablesRepository.save(entity);
        } else {
            throw new RuntimeException("Table not found with id: " + id);
        }
    }

    public ManageTables findByNo(String tableNo) {
        Optional<ManageTables> optional = manageTablesRepository.findByTableNo(tableNo);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new RuntimeException("Table not found: " + tableNo);
    }

    public TableStatsDTO getTableStats() {
        List<ManageTables> allTables = manageTablesRepository.findAll();
        
        TableStatsDTO stats = new TableStatsDTO();
        stats.tables = new ArrayList<>();
        stats.total = allTables.size();
        stats.free = allTables.stream().filter(t -> "Available".equals(t.getStatus())).count();
        stats.busy = allTables.stream().filter(t -> !"Available".equals(t.getStatus())).count();
        
        return stats;
    }

    public List<ManageTablesDTO> getAllTables() {
        List<ManageTables> tables = manageTablesRepository.findAll();
        List<Reservation> activeReservations = reservationRepository.findByStatusIn(List.of("Pending", "Confirm", "Seated", "Completed"));
        
        Map<Long, Reservation> tableToReservation = activeReservations.stream()
            .filter(r -> r.getTable() != null)
            .collect(Collectors.toMap(
                r -> r.getTable().getId(),
                r -> r,
                (r1, r2) -> r1
            ));
        
        List<ManageTablesDTO> dtos = new ArrayList<>();
        
        for (ManageTables table : tables) {
            ManageTablesDTO dto = new ManageTablesDTO();
            dto.setTableNo(table.getTableNo());
            dto.setStatus(table.getStatus());
            dto.setCapacity(table.getCapacity());
            dto.setLocation(table.getLocation());
            dto.setTableId(table.getId());
            dto.setTablestarttime(table.getTablestarttime());
            dto.setTableendtime(table.getTableendtime());
            
            Reservation r = tableToReservation.get(table.getId());
            if (r != null) {
                dto.setCustomer(r.getCustomer() != null ? r.getCustomer().getName() : null);
                dto.setPhone(r.getCustomer() != null ? r.getCustomer().getPhone() : null);
                dto.setEmail(r.getCustomer() != null ? r.getCustomer().getEmail() : null);
                dto.setPax(r.getPax());
                dto.setReference(r.getReference());
                dto.setPrefer(r.getPrefer());
                dto.setReservationPendingtime(r.getReservationPendingtime());
                dto.setReservationConfirmtime(r.getReservationConfirmtime());
                dto.setReservationSeatedtime(r.getReservationSeatedtime());
                dto.setReservationCompletetime(r.getReservationCompletetime());
                dto.setSales(r.getSales());
            }
            
            dtos.add(dto);
        }
        
        return dtos;
    }
    
    @Transactional
    public void clearTableFromReservation(String reference) {
        if (reference != null && !reference.isEmpty()) {
            Optional<Reservation> optional = reservationRepository.findByReference(reference);
            if (optional.isPresent()) {
                Reservation reservation = optional.get();
                reservation.setTable(null);
                reservation.setStatus("Pending");
                reservationRepository.save(reservation);
            }
        }
    }

    @Transactional
    public void clearReservationsForTableById(Long tableId) {
        List<Reservation> reservations = reservationRepository.findByTable_Id(tableId);
        for (Reservation reservation : reservations) {
            reservation.setTable(null);
            reservation.setStatus("Pending");
            reservationRepository.save(reservation);
        }
    }
    
    @Transactional
    public void clearReservationByTableId(Long tableId) {
        List<Reservation> reservations = reservationRepository.findByTable_Id(tableId);
        for (Reservation reservation : reservations) {
            if (!"Complete".equals(reservation.getStatus()) && !"Cancelled".equals(reservation.getStatus())) {
                reservation.setTable(null);
                reservation.setStatus("Pending");
                reservationRepository.save(reservation);
            }
        }
    }

    public void clearReservationsForTable(Long tableId) {
        clearReservationsForTableById(tableId);
    }
}
