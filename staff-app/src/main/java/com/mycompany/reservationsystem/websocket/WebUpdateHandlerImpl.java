package com.mycompany.reservationsystem.websocket;

import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.controller.main.AdministratorUIController;
import com.mycompany.reservationsystem.controller.main.DashboardController;
import com.mycompany.reservationsystem.controller.main.ReservationController;
import com.mycompany.reservationsystem.controller.main.TableController;
import com.mycompany.reservationsystem.dto.WebUpdateDTO;
import com.mycompany.reservationsystem.service.MessageDispatchService;
import com.mycompany.reservationsystem.service.MessageService;
import com.mycompany.reservationsystem.util.NotificationManager;
import javafx.application.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebUpdateHandlerImpl implements WebSocketListener {
    private final MessageService messageService = null;
    private final MessageDispatchService messageDispatchService = null;

    private AdministratorUIController adminController;

    public void setAdminController(AdministratorUIController controller) {
        this.adminController = controller;
    }

    @Override
    public void onMessage(WebUpdateDTO dto) {
        if (dto == null || dto.getCode() == null) {
            return;
        }

        DashboardController dashboard = adminController.getDashboardController();
        ReservationController reservation = adminController.getReservationController();
        TableController table = getTableController();
        if (dashboard == null) return;

        final String code = dto.getCode();
        final String message = dto.getMessage() != null ? dto.getMessage() : "";
        final String phone = dto.getPhone();

        /* ================= UI THREAD ================= */
        Platform.runLater(() -> {

            System.out.println("WS EVENT: " + code);

            // Only show notification for new reservations
            switch (code) {
                case "NEW_RESERVATION":
                case "reservation": {
                    adminController.addNotification(
                            "New Reservation",
                            message,
                            AdministratorUIController.NotificationType.SUCCESS,
                            dto.getReference()
                    );
                    break;
                }
            }

            dashboard.loadRecentReservations();
            dashboard.updateLabels();
            reservation.loadReservationsData();
            table.loadTableManager();
        });

        /* ================= MESSAGE THREAD ================= */
        if (phone != null && !phone.isEmpty()) {
            final String phoneNumber = phone;
            new Thread(() -> {
                try {
                    switch (code) {
                        case "NEW_RESERVATION":
                        case "reservation": {
                            sendConfiguredMessage(phoneNumber, "message.new");
                            break;
                        }

                        case "CANCELLED_RESERVATION": {
                            sendConfiguredMessage(phoneNumber, "message.cancelled");
                            break;
                        }
                        case "CONFIRM_RESERVATION":
                        case "status": {
                            if (message != null && message.toLowerCase().contains("cancelled")) {
                                sendConfiguredMessage(phoneNumber, "message.cancelled");
                            } else {
                                sendConfiguredMessage(phoneNumber, "message.confirm");
                            }
                            break;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }, "MESSAGE-DISPATCH-THREAD").start();
        }
    }

    private TableController getTableController() {
        return adminController.getTableController();
    }

    /* ================= SAFE SEND ================= */
    private void sendSafe(String phone, String message) {
        try {
            MessageDispatchService.send(phone, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendConfiguredMessage(String phone, String key) {
        if (!AppSettings.loadMessageEnabled(key)) {
            return;
        }

        String label = AppSettings.loadMessageLabel(key);
        if (label == null || label.isBlank()) {
            return;
        }

        MessageService
                .findByLabel(label)
                .ifPresent(msg -> sendSafe(phone, msg.getMessageDetails()));
    }
}
