package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.model.ActivityLog;
import com.mycompany.reservationsystem.repository.ActivityLogRepository;
import com.mycompany.reservationsystem.util.ActivityLogSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    /**
     * Get logs filtered by date with pagination
     */
    public Page<ActivityLog> getLogsByDate(LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        return activityLogRepository.findAll(ActivityLogSpecification.byDate(dateFrom, dateTo), pageable);
    }

    /**
     * Log a user action into the activity_logs table.
     */
    public void logAction(String user, String position, String module, String action, String description) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setPosition(position); // set the position
        log.setModule(module);
        log.setAction(action);
        log.setDescription(description);
        log.setTimestamp(LocalDateTime.now());

        activityLogRepository.save(log);
    }

    /**
     * Optional: clear all logs (useful for maintenance)
     */
    public void clearLogs() {
        activityLogRepository.deleteAll();
    }

    public Page<ActivityLog> searchActivityLogs(String query, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (startDate != null && endDate != null) {
            return activityLogRepository.filterByDate(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay(),
                pageable
            );
        }
        return activityLogRepository.findAll(pageable);
    }
}
