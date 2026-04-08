package com.mycompany.reservationsystem.util;

import com.mycompany.reservationsystem.model.ActivityLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ActivityLogSpecification {

    public static Specification<ActivityLog> byDate(LocalDateTime dateFrom, LocalDateTime dateTo) {
        return (root, query, cb) -> cb.between(root.get("timestamp"), dateFrom, dateTo);
    }

    public static Specification<ActivityLog> byUser(String user) {
        return (root, query, cb) -> cb.equal(root.get("user"), user);
    }

    public static Specification<ActivityLog> byAction(String action) {
        return (root, query, cb) -> cb.equal(root.get("action"), action);
    }

    public static Specification<ActivityLog> byModule(String module) {
        return (root, query, cb) -> cb.equal(root.get("module"), module);
    }
}
