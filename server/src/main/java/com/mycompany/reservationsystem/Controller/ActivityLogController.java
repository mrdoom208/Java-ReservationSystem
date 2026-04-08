package com.mycompany.reservationsystem.Controller;

import com.mycompany.reservationsystem.model.ActivityLog;
import com.mycompany.reservationsystem.repository.ActivityLogRepository;
import com.mycompany.reservationsystem.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/activity-logs")
public class ActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @PostMapping
    public ActivityLog createActivityLog(@RequestBody ActivityLog activityLog) {
        activityLog.setTimestamp(LocalDateTime.now());
        return activityLogRepository.save(activityLog);
    }

    @PostMapping("/all")
    public Page<ActivityLog> getAllActivityLogs(@RequestBody PageRequest request) {
        return activityLogRepository.findAll(org.springframework.data.domain.PageRequest.of(request.getPage(), request.getSize(), Sort.by(Sort.Direction.DESC, "timestamp")));
    }

    @GetMapping("/count")
    public long getActivityLogCount() {
        return activityLogRepository.count();
    }

    @PostMapping("/get")
    public ActivityLog getActivityLogById(@RequestBody IdRequest request) {
        return activityLogRepository.findById(request.getId()).orElse(null);
    }

    @PostMapping("/by-date")
    public Page<ActivityLog> getActivityLogsByDate(@RequestBody DatePageRequest request) {
        LocalDateTime dateFrom = request.getDate().atStartOfDay();
        LocalDateTime dateTo = request.getDate().plusDays(1).atStartOfDay();
        return activityLogRepository.filterByDate(dateFrom, dateTo, org.springframework.data.domain.PageRequest.of(request.getPage(), request.getSize()));
    }

    @PostMapping("/by-user")
    public Page<ActivityLog> getActivityLogsByUser(@RequestBody UserPageRequest request) {
        return activityLogRepository.findByUser(request.getUser(), org.springframework.data.domain.PageRequest.of(request.getPage(), request.getSize(), Sort.by(Sort.Direction.DESC, "timestamp")));
    }

    @PostMapping("/by-action")
    public Page<ActivityLog> getActivityLogsByAction(@RequestBody ActionPageRequest request) {
        return activityLogRepository.findByActionContainingIgnoreCase(request.getAction(), org.springframework.data.domain.PageRequest.of(request.getPage(), request.getSize(), Sort.by(Sort.Direction.DESC, "timestamp")));
    }

    @PostMapping("/search")
    public Page<ActivityLog> searchActivityLogs(@RequestBody SearchPageRequest request) {
        return activityLogService.searchActivityLogs(request.getQuery(), request.getStartDate(), request.getEndDate(), org.springframework.data.domain.PageRequest.of(request.getPage(), request.getSize()));
    }

    @DeleteMapping("/{id}")
    public void deleteActivityLog(@PathVariable Long id) {
        activityLogRepository.deleteById(id);
    }

    public static class IdRequest {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    public static class DateRequest {
        private LocalDate date;
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
    }

    public static class PageRequest {
        private int page = 0;
        private int size = 20;
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }

    public static class DatePageRequest {
        private LocalDate date;
        private int page = 0;
        private int size = 20;
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }

    public static class UserPageRequest {
        private String user;
        private int page = 0;
        private int size = 20;
        public String getUser() { return user; }
        public void setUser(String user) { this.user = user; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }

    public static class ActionPageRequest {
        private String action;
        private int page = 0;
        private int size = 20;
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }

    public static class SearchPageRequest {
        private String query;
        private LocalDate startDate;
        private LocalDate endDate;
        private int page = 0;
        private int size = 20;
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }
}
