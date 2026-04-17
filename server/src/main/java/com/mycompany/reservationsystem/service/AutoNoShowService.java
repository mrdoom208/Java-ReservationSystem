package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
public class AutoNoShowService {

    private static final Logger logger = LoggerFactory.getLogger(AutoNoShowService.class);

    private final ReservationRepository reservationRepository;
    private final SmsService smsService;

    public AutoNoShowService(ReservationRepository reservationRepository, SmsService smsService) {
        this.reservationRepository = reservationRepository;
        this.smsService = smsService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoNoShowConfirmedReservations() {
        int noshowMinutes = AppSettings.loadNoshowTimeMinutes();
        
        if (noshowMinutes <= 0) {
            return;
        }

        List<Reservation> confirmReservations = reservationRepository.findByStatus("Confirm");
        LocalTime now = LocalTime.now();
        int noshowCount = 0;

        for (Reservation reservation : confirmReservations) {
            if (reservation.getReservationConfirmtime() == null) {
                continue;
            }

            long minutesSinceConfirmed = java.time.Duration.between(reservation.getReservationConfirmtime(), now).toMinutes();

            if (minutesSinceConfirmed >= noshowMinutes) {
                markAsNoShow(reservation, now);
                noshowCount++;
            }
        }

        if (noshowCount > 0) {
            logger.info("Auto-marked {} reservations as No Show due to timeout", noshowCount);
        }
    }

    private void markAsNoShow(Reservation reservation, LocalTime noshowTime) {
        reservation.setStatus("No Show");
        reservation.setReservationNoshowtime(noshowTime);
        reservationRepository.save(reservation);

        String message = "Your reservation (Ref: " + reservation.getReference() + ") has been marked as No Show "
                + "as you did not arrive within the allocated time. "
                + "Please call the restaurant if you wish to rebook. Thank you!";
        
        try {
            smsService.sendSms(reservation.getCustomer().getPhone(), message);
        } catch (Exception e) {
            logger.error("Failed to send No Show SMS to {}: {}", 
                    reservation.getCustomer().getPhone(), e.getMessage());
        }

        logger.info("Marked reservation as No Show: {} for customer: {}", 
                reservation.getReference(), reservation.getCustomer().getName());
    }
}
