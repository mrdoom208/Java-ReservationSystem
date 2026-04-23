package com.mycompany.reservationsystem.Controller;

import com.mycompany.reservationsystem.dto.CustomerReportDTO;
import com.mycompany.reservationsystem.dto.SalesReportsDTO;
import com.mycompany.reservationsystem.dto.TableUsageReportDTO;
import com.mycompany.reservationsystem.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    @Autowired
    private ReservationRepository reservationRepository;

    @PostMapping("/sales")
    public List<SalesReportsDTO> getSalesReports(@RequestBody DateRangeRequest request) {
        return reservationRepository.getSalesReports(request.getFrom(), request.getTo());
    }

    @PostMapping("/table-usage")
    public List<TableUsageReportDTO> getTableUsageReports(@RequestBody DateRangeRequest request) {
        if (request.getFrom() != null && request.getTo() != null) {
            List<TableUsageReportDTO> result = new java.util.ArrayList<>();
            for (LocalDate date = request.getFrom(); !date.isAfter(request.getTo()); date = date.plusDays(1)) {
                result.addAll(reservationRepository.getTableUsageReport(date));
            }
            return result;
        }
        return reservationRepository.getTableUsageReport(LocalDate.now());
    }

    @PostMapping("/customers")
    public Page<CustomerReportDTO> getCustomerReports(@RequestBody CustomerPageRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return reservationRepository.getFilteredCustomerReportPaged(request.getFrom(), request.getTo(), pageable);
    }

    public static class DateRangeRequest {
        private LocalDate from;
        private LocalDate to;
        public LocalDate getFrom() { return from; }
        public void setFrom(LocalDate from) { this.from = from; }
        public LocalDate getTo() { return to; }
        public void setTo(LocalDate to) { this.to = to; }
    }

    public static class CustomerPageRequest {
        private LocalDate from;
        private LocalDate to;
        private int page = 0;
        private int size = 20;
        public LocalDate getFrom() { return from; }
        public void setFrom(LocalDate from) { this.from = from; }
        public LocalDate getTo() { return to; }
        public void setTo(LocalDate to) { this.to = to; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }
}
