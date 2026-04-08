package com.mycompany.reservationsystem.util;

import com.mycompany.reservationsystem.model.ActivityLog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ActivityLogSpecification {

    public static List<ActivityLog> byDate(
            List<ActivityLog> logs,
            LocalDateTime from,
            LocalDateTime to
    ) {
        List<ActivityLog> result = new ArrayList<>();
        
        for (ActivityLog log : logs) {
            boolean matches = true;
            
            if (from != null) {
                LocalDateTime timestamp = log.getTimestamp();
                if (timestamp == null || timestamp.isBefore(from)) {
                    matches = false;
                }
            }
            
            if (to != null && matches) {
                LocalDateTime timestamp = log.getTimestamp();
                if (timestamp == null || timestamp.isAfter(to)) {
                    matches = false;
                }
            }
            
            if (matches) {
                result.add(log);
            }
        }
        
        return result;
    }
}
