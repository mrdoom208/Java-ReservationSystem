package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.repository.ReservationRepository;
import com.mycompany.reservationsystem.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
public class AutoCancelService {

    private static final Logger logger = LoggerFactory.getLogger(AutoCancelService.class);

    private final ReservationRepository reservationRepository;
    private final SmsService smsService;

    public AutoCancelService(ReservationRepository reservationRepository, SmsService smsService) {
        this.reservationRepository = reservationRepository;
        this.smsService = smsService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoCancelConfirmedReservations() {
        String cancelTimeSetting = AppSettings.loadCancelTime();
        
        if (cancelTimeSetting == null || cancelTimeSetting.isBlank() 
                || cancelTimeSetting.equals("Never") 
                || cancelTimeSetting.equals("Until Table is Available")) {
            return;
        }

        int cancelMinutes = parseCancelMinutes(cancelTimeSetting);
        if (cancelMinutes <= 0) {
            return;
        }

        List<Reservation> awaitingReservations = reservationRepository.findConfirmedAwaitingArrival();

        LocalTime now = LocalTime.now();
        int cancelledCount = 0;

        for (Reservation reservation : awaitingReservations) {
            if (reservation.getReservationNotifiedtime() == null) {
                continue;
            }

            long minutesSinceNotified = java.time.Duration.between(reservation.getReservationNotifiedtime(), now).toMinutes();

            if (minutesSinceNotified >= cancelMinutes) {
                cancelReservation(reservation, now);
                cancelledCount++;
            }
        }

        if (cancelledCount > 0) {
            logger.info("Auto-cancelled {} reservations that exceeded notification timeout", cancelledCount);
        }
    }

    private int parseCancelMinutes(String cancelTimeSetting) {
        try {
            String cleaned = cancelTimeSetting.toLowerCase().replace("minutes", "").replace("minute", "").trim();
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            logger.warn("Invalid cancel time setting: {}", cancelTimeSetting);
            return 0;
        }
    }

    private void cancelReservation(Reservation reservation, LocalTime cancelTime) {
        reservation.setStatus("Cancelled");
        reservation.setReservationCancelledtime(cancelTime);
        reservationRepository.save(reservation);

        String message = "Your reservation has been cancelled due to no response. "
                + "Please call the restaurant to rebook. Thank you!";
        
        try {
            smsService.sendSms(reservation.getCustomer().getPhone(), message);
        } catch (Exception e) {
            logger.error("Failed to send cancellation SMS to {}: {}", 
                    reservation.getCustomer().getPhone(), e.getMessage());
        }

        logger.info("Auto-cancelled reservation: {} for customer: {}", 
                reservation.getReference(), reservation.getCustomer().getName());
    }
}