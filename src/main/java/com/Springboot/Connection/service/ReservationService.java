package com.Springboot.Connection.service;

import com.Springboot.Connection.dto.WebUpdateDTO;
import com.Springboot.Connection.model.Reservation;
import com.Springboot.Connection.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SettingsService settingsService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository,
                              SettingsService settingsService) {
        this.reservationRepository = reservationRepository;
        this.settingsService = settingsService;
    }

    @Scheduled(fixedRate = 60000) // Every 1 minute
    public void checkAndUpdateReservationStatuses() {
        long autoCancelMinutes = settingsService.getAutoCancelMinutes();
        System.out.println("Auto-cancel minutes: " + autoCancelMinutes);

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        // Pending reservations → Cancelled
        List<Reservation> pendingReservations = reservationRepository.findAllByStatus("Pending");
        for (Reservation res : pendingReservations) {
            if (res.getReservationNotifiedtime() != null && res.getDate() != null) {
                LocalDateTime notifiedDateTime = LocalDateTime.of(res.getDate(), res.getReservationNotifiedtime());
                if (Duration.between(notifiedDateTime, now).toMinutes() > autoCancelMinutes) {
                    handlePendingCancellationAsync(res);
                }
            }
        }

        // Confirmed reservations → No Show
        List<Reservation> confirmedReservations = reservationRepository.findAllByStatus("Confirm");
        for (Reservation res : confirmedReservations) {
            if (res.getReservationConfirmtime() != null && res.getDate() != null) {
                LocalDateTime confirmDateTime = LocalDateTime.of(res.getDate(), res.getReservationConfirmtime());
                if (Duration.between(confirmDateTime, now).toMinutes() > autoCancelMinutes) {
                    handleNoShowAsync(res);
                }
            }
        }
    }

    @Async
    public void handlePendingCancellationAsync(Reservation reservation) {
        reservation.setStatus("Cancelled");
        reservation.setReservationCancelledtime(LocalDateTime.now(ZoneOffset.UTC).toLocalTime());
        reservation.setRevenue(BigDecimal.ZERO);
        reservationRepository.save(reservation);

        sendWebSocketNotification(reservation, "CANCELLED_RESERVATION");
    }

    @Async
    public void handleNoShowAsync(Reservation reservation) {
        reservation.setStatus("No Show");
        reservation.setReservationNoshowtime(LocalDateTime.now(ZoneOffset.UTC).toLocalTime());
        reservation.setRevenue(BigDecimal.ZERO);
        reservationRepository.save(reservation);

        sendWebSocketNotification(reservation, "CANCELLED_RESERVATION");
    }

    private void sendWebSocketNotification(Reservation reservation, String code) {
        WebUpdateDTO dto = new WebUpdateDTO();
        dto.setCode(code);
        dto.setMessage("Reservation from " + reservation.getCustomer().getName()
                + " (" + reservation.getPax() + " pax) | Ref: "
                + reservation.getReference() + " has been " + reservation.getStatus().toLowerCase());
        dto.setPhone(reservation.getCustomer().getPhone());
        dto.setReference(reservation.getReference());

        messagingTemplate.convertAndSend("/topic/forms", dto);
        System.out.println("Reservation update: " + dto.getCode());
    }

    @Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
    public void deleteOldReservations() {
        int months = settingsService.getAutoDeleteMonths();
        reservationRepository.deleteByDateBefore(java.time.LocalDate.now(ZoneOffset.UTC).minusMonths(months));
        System.out.println("Deleted old reservations older than " + months + " months");
    }
}
