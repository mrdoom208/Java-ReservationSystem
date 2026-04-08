package com.mycompany.reservationsystem.util;

import com.mycompany.reservationsystem.model.Reservation;

import java.util.HashMap;
import java.util.Map;

public class NotificationUtil {

    // Map of reservationId -> last notify timestamp (ms)
    private static final Map<Long, Long> cooldownMap = new HashMap<>();

    private NotificationUtil() {} // prevent instantiation

    /** Start cooldown for a reservation */
    public static void markNotified(Reservation reservation) {
        if (reservation == null || reservation.getId() == null) return;
        cooldownMap.put(reservation.getId(), System.currentTimeMillis());
    }

    /** Returns remaining seconds of cooldown for a reservation, or 0 if finished */
    public static int getRemainingCooldown(Reservation reservation, int cooldownSeconds) {
        if (reservation == null || reservation.getId() == null) return 0;
        Long lastSent = cooldownMap.get(reservation.getId());
        if (lastSent == null) return 0;

        long elapsed = (System.currentTimeMillis() - lastSent) / 1000;
        int remaining = cooldownSeconds - (int) elapsed;
        return Math.max(remaining, 0);
    }

    /** Checks if reservation is still in cooldown */
    public static boolean isInCooldown(Reservation reservation, int cooldownSeconds) {
        return getRemainingCooldown(reservation, cooldownSeconds) > 0;
    }
}