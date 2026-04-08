package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.api.ApiClient;

public class WebsiteSyncService {

    public static void syncTables() {
        ApiClient.getTables();
    }

    public static void syncReservations() {
        ApiClient.getReservations();
    }

    public static void sendAutoCancelTime(int minutes) {
        System.out.println("Website sync: Auto cancel time set to " + minutes + " minutes");
    }

    public static void sendAutoDeleteMonths(int months) {
        System.out.println("Website sync: Auto delete months set to " + months);
    }
}
