package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.dto.CustomerReportDTO;
import com.mycompany.reservationsystem.dto.TableUsageReportDTO;
import com.mycompany.reservationsystem.model.ManageTables;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.repository.ManageTablesRepository;
import com.mycompany.reservationsystem.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private ManageTablesRepository manageTablesRepository;

    @Transactional
    public void updateStatus(String Ref, String newStatus) {
        Optional<Reservation> optional = reservationRepository.findByReference(Ref);
        if (optional.isPresent()) {
            Reservation entity = optional.get();
            entity.setStatus(newStatus);
            
            if ("Cancelled".equals(newStatus) && entity.getTable() != null) {
                ManageTables table = entity.getTable();
                entity.setTable(null);
                table.setStatus("Available");
                table.setTablestarttime(null);
                manageTablesRepository.save(table);
            }
            
            reservationRepository.save(entity);
        } else {
            throw new RuntimeException("Reservation not found with id: " + Ref);
        }
    }

    @Transactional
    public void updateTableId(String Ref, ManageTables table) {
        Optional<Reservation> optional = reservationRepository.findByReference(Ref);
        if (optional.isPresent()) {
            Reservation entity = optional.get();
            entity.setTable(table);
            entity.setStatus("Confirmed");
            entity.setReservationConfirmtime(LocalTime.now());
            reservationRepository.save(entity);
        } else {
            throw new RuntimeException("Reservation not found with id table: " + Ref);
        }
    }

    @Transactional
    public void clearTableFromReservation(String Ref) {
        Optional<Reservation> optional = reservationRepository.findByReference(Ref);
        if (optional.isPresent()) {
            Reservation entity = optional.get();
            entity.setTable(null);
            entity.setStatus("Pending");
            reservationRepository.save(entity);
        }
    }

    @Transactional
    public void updateSeatedtime(String Ref, LocalTime newtime) {
        Optional<Reservation> optional = reservationRepository.findByReference(Ref);
        if (optional.isPresent()) {
            Reservation entity = optional.get();
            entity.setReservationSeatedtime(newtime);
            reservationRepository.save(entity);
        } else {
            throw new RuntimeException("Reservation not found" + Ref);
        }
    }

    @Transactional
    public void updateCompletetime(String Ref, LocalTime newtime) {
        Optional<Reservation> optional = reservationRepository.findByReference(Ref);
        if (optional.isPresent()) {
            Reservation entity = optional.get();
            entity.setReservationCompletetime(newtime);
            reservationRepository.save(entity);
        } else {
            throw new RuntimeException("Reservation not found" + Ref);
        }
    }

    @Transactional
    public void updateFinishedTime(String Ref, LocalTime time) {
        updateCompletetime(Ref, time);
    }

    @Transactional
    public void updateAmountPaid(String Ref, BigDecimal amount) {
        Optional<Reservation> optional = reservationRepository.findByReference(Ref);
        if (optional.isPresent()) {
            Reservation entity = optional.get();
            entity.setSales(amount);
            reservationRepository.save(entity);
        }
    }

    @Transactional
    public void updateTotalAmount(String Ref, BigDecimal amount) {
        updateAmountPaid(Ref, amount);
    }

    @Transactional
    public void deleteByReference(String reference) {
        Optional<Reservation> optional = reservationRepository.findByReference(reference);
        optional.ifPresent(reservationRepository::delete);
    }

    @Transactional
    public boolean setSalesForReference(String reference, BigDecimal sales) {
        int updatedRows = reservationRepository.updateSalesByReference(reference, sales);
        return updatedRows > 0;
    }

    @Transactional
    public boolean setStatusForReference(String reference, String status) {
        int updatedRows = reservationRepository.updateStatusByReference(reference, status);
        return updatedRows > 0;
    }

    public List<CustomerReportDTO> loadPage(int page, int pageSize) {
        return reservationRepository.getAllCustomerReport(PageRequest.of(page, pageSize));
    }

    public List<CustomerReportDTO> loadByDate(LocalDate from, LocalDate to) {
        return reservationRepository.getFilteredCustomerReport(from, to);
    }

    public List<String> getAllCustomerPhones() {
        return reservationRepository.findAllCustomerPhones();
    }

    public List<Reservation> findByDate(LocalDate date) {
        return reservationRepository.findByDate(date);
    }

    public List<Reservation> findByStatus(String status) {
        return reservationRepository.findByStatus(status);
    }

    public List<Reservation> findActiveReservations() {
        return reservationRepository.findByStatusIn(List.of("Pending", "Confirmed", "Seated"));
    }

    public List<CustomerReportDTO> getCustomerReport(LocalDate startDate, LocalDate endDate) {
        return reservationRepository.getFilteredCustomerReport(startDate, endDate);
    }

    public List<TableUsageReportDTO> getTableUsageReport(LocalDate date) {
        return reservationRepository.getTableUsageReport(date);
    }

    public Map<String, Object> getDashboardCounts() {
        Map<String, Object> counts = new HashMap<>();
        LocalDate today = LocalDate.now();

        long totalReservations = reservationRepository.count();
        long todayReservations = reservationRepository.findByDate(today).size();
        long pendingCount = reservationRepository.countByStatus("Pending");
        long confirmedCount = reservationRepository.countByStatus("Confirmed");
        long seatedCount = reservationRepository.countByStatus("Seated");
        long finishedCount = reservationRepository.countByStatus("Complete");
        long cancelledCount = reservationRepository.countByStatus("Cancelled");
        long noshowCount = reservationRepository.countByStatus("No Show");

        counts.put("totalReservations", totalReservations);
        counts.put("todayReservations", todayReservations);
        counts.put("pendingCount", pendingCount);
        counts.put("confirmedCount", confirmedCount);
        counts.put("seatedCount", seatedCount);
        counts.put("finishedCount", finishedCount);
        counts.put("cancelledCount", cancelledCount);
        counts.put("noshowCount", noshowCount);

        return counts;
    }
}
