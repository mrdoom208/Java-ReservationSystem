package com.mycompany.reservationsystem.Controller;

import com.mycompany.reservationsystem.dto.WebUpdateDTO;
import com.mycompany.reservationsystem.model.Customer;
import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.repository.CustomerRepository;
import com.mycompany.reservationsystem.repository.ReservationRepository;
import com.mycompany.reservationsystem.service.SmsService;
import com.mycompany.reservationsystem.service.WebSocketBroadcastService;
import com.mycompany.reservationsystem.util.PhoneFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Controller
public class ReserveController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SmsService smsService;

    @Autowired
    private WebSocketBroadcastService webSocketBroadcastService;

    @PostMapping("/reserve")
    public String createReservation(
            @RequestParam String Name,
            @RequestParam String Phone,
            @RequestParam String pax,
            @RequestParam(required = false) String Prefer,
            @RequestParam(required = false) String Email
    ) {
        String normalizedPhone = PhoneFormatter.normalizePHNumber(Phone);
        
        Customer customer = new Customer();
        customer.setName(Name);
        customer.setPhone(normalizedPhone);
        if (Email != null && !Email.isBlank()) {
            customer.setEmail(Email);
        }
        customer = customerRepository.save(customer);

        String reference = generateReference();
        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setPax(Integer.parseInt(pax));
        reservation.setPrefer(Prefer);
        reservation.setReference(reference);
        reservation.setStatus("Pending");
        reservation.setDate(LocalDate.now());
        reservation.setReservationPendingtime(LocalTime.now());
        reservation = reservationRepository.save(reservation);

        String message = String.format(
                "Hello %s, your reservation has been successfully made.\nReference: %s\nParty Size: %s\nWe are looking forward to welcoming you!",
                Name, reference, pax
        );
        smsService.sendSms(normalizedPhone, message);

        WebUpdateDTO dto = new WebUpdateDTO();
        dto.setCode("reservation");
        dto.setMessage("New reservation created: " + reference);
        dto.setType("reservation");
        dto.setPhone(normalizedPhone);
        dto.setReference(reference);
        dto.setPax(Integer.parseInt(pax));
        dto.setCustomerName(Name);
        try {
            webSocketBroadcastService.broadcastToFrontend(dto);
        } catch (Exception e) {
            // Log but don't fail reservation creation
        }

        return "redirect:/login?phone=" + normalizedPhone + "&reference=" + reference + "&success=Reservation+Created+Successfully";
    }

    private String generateReference() {
        return "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}