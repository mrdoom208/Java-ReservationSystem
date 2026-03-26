package com.Springboot.Connection.Controller;

import com.Springboot.Connection.Config.WebSocketBroadcaster;
import com.Springboot.Connection.dto.NotificationDTO;
import com.Springboot.Connection.dto.WebUpdateDTO;
import com.Springboot.Connection.model.Notification;
import com.Springboot.Connection.model.Reservation;
import com.Springboot.Connection.repository.NotificationRepository;
import com.Springboot.Connection.repository.ReservationRepository;
import com.Springboot.Connection.service.SmsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class QueueController {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private NotificationRepository notificationRepository;



    @Autowired
    private WebSocketBroadcaster broadcaster;

    @Autowired
    private SimpUserRegistry simpUserRegistry;
    
    @Autowired
    SimpMessagingTemplate messagingTemplate;


    @Autowired
    SmsService smsService;

    @PostMapping("/login")
    public String login(@RequestParam String phone,@RequestParam String reference, HttpSession session) {
        Reservation reservation = reservationRepository.findByCustomerPhoneAndReference(phone, reference);

        if (reservation != null) {
            session.setAttribute("phone", reservation.getCustomer().getPhone());
            session.setAttribute("reference", reservation.getReference());
            return "redirect:/queue";
        } else {
            return "redirect:/loginpage?error=No Reservation Found";
        }

    }
    @GetMapping("/login")
    public String loginViaLink(
            @RequestParam("phone") String phone,
            @RequestParam("reference") String reference,
            HttpSession session
    ) {
        // Find reservation
        Reservation reservation = reservationRepository.findByCustomerPhoneAndReference(phone, reference);

        if (reservation != null) {
            // Store session info
            session.setAttribute("phone", reservation.getCustomer().getPhone());
            session.setAttribute("reference", reservation.getReference());
            session.setAttribute("reservationId", reservation.getId());

            // Redirect to reservation page
            return "redirect:/queue"; // your page to view/modify reservation
        } else {
            return "redirect:/loginpage?error=No+Reservation+Found";
        }
    }

    @GetMapping("/queue")
    public String queuePage(HttpSession session, Model model) {
        String account = (String) session.getAttribute("reference"); // Spring Security username

        // Send all pending notifications
        String phone = (String) session.getAttribute("phone");
        String reference = (String) session.getAttribute("reference");

        if (phone == null) {
            return "redirect:/loginpage";
        }

        Reservation reservation = reservationRepository.findByCustomerPhoneAndReference(phone, reference);

        if (reservation != null) {
            long position = reservationRepository.countAhead(reservation.getReservationPendingtime()) + 1;
            long Minutes = 10;
            long estimatedMinutes = Minutes * position;
            long hours = estimatedMinutes / 60;
            long minutes = estimatedMinutes % 60;
            String estimatedTimeStr = String.format("%02d:%02d", hours, minutes);

            LocalDate pendingDate = reservation.getDate();   // e.g., 2025-12-12
            LocalTime pendingTime = reservation.getReservationPendingtime();   // e.g., 23:58:00
            LocalDateTime pendingDateTime = LocalDateTime.of(pendingDate, pendingTime);

            LocalDateTime completeDateTime = null;
            LocalDateTime cancelledDateTime = null;
            LocalDateTime NoShowDateTime = null;

            if (reservation.getReservationCompletetime() != null) {
                completeDateTime = LocalDateTime.of(reservation.getDate(), reservation.getReservationCompletetime());
            }
            if (reservation.getReservationCancelledtime() != null) {
                cancelledDateTime = LocalDateTime.of(reservation.getDate(), reservation.getReservationCancelledtime());
            }
            if (reservation.getReservationNoshowtime() != null) {
                NoShowDateTime = LocalDateTime.of(reservation.getDate(), reservation.getReservationNoshowtime());
            }

            // Format for display with timezone awareness
            // Assuming your server times are in UTC, convert to ISO 8601 format with 'Z' indicator
            DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

            // For the "Created" display, format it nicely but client will convert to local
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String createdDisplay = pendingDateTime.format(displayFormatter);

            model.addAttribute("reservationId", reservation.getId());
            model.addAttribute("customerName", reservation.getCustomer().getName());
            model.addAttribute("customerPhone", reservation.getCustomer().getPhone());
            model.addAttribute("reference", reservation.getReference());
            model.addAttribute("queueNumber", position);
            model.addAttribute("seatingCapacity", reservation.getPax());
            model.addAttribute("preferredSpace", reservation.getPrefer() != null ? reservation.getPrefer() : "");
            model.addAttribute("status", reservation.getStatus());

            // Send as ISO format strings - JavaScript will handle timezone conversion
            model.addAttribute("pendingDateTime", pendingDateTime.atOffset(ZoneOffset.UTC).format(isoFormatter));
            model.addAttribute("completeDateTime",
                    reservation.getReservationCompletetime() != null ? completeDateTime.atOffset(ZoneOffset.UTC).format(isoFormatter) : "");
            model.addAttribute("cancelledDateTime",
                    reservation.getReservationCancelledtime() != null ? cancelledDateTime.atOffset(ZoneOffset.UTC).format(isoFormatter) : "");
            model.addAttribute("noShowDateTime",
                    reservation.getReservationNoshowtime() != null ? NoShowDateTime.atOffset(ZoneOffset.UTC).format(isoFormatter) : "");

            // Send the created time in simple format - JS will convert to local timezone
            model.addAttribute("created", createdDisplay);

            int activeSegments = switch (reservation.getStatus()) {
                case "Pending"  -> 6;
                case "Confirm"  -> 12;
                case "Seated"   -> 18;
                case "Complete" -> 24;
                case "Cancelled" -> 24;
                default -> 0;
            };
            model.addAttribute("activeSegments", activeSegments);

        } else {
            // Default placeholders
            model.addAttribute("customerName", "Loading...");
            model.addAttribute("customerPhone", "Loading...");
            model.addAttribute("reference", "Loading...");
            model.addAttribute("queueNumber", "Loading...");
            model.addAttribute("seatingCapacity", "Loading...");
            model.addAttribute("preferredSpace", "Loading...");
            model.addAttribute("estimatedTime", "Loading...");
        }

        return "ReservationData";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Invalidate the session
        session.invalidate();

        // Redirect to login page
        return "redirect:/loginpage?logout=You have Successfully logged out";
    }

    @PostMapping("/updateCustomer")
    public String updateCustomer(@RequestParam String customerName,@RequestParam String customerPhone,@RequestParam String seatingCapacity,@RequestParam String preferredSpace, HttpSession session) {
        String phone = (String) session.getAttribute("phone");
        String reference = (String) session.getAttribute("reference");

        if (phone == null || reference == null) {
            // session expired or user not logged in
            return "redirect:/loginpage";
        }

        Reservation reservation = reservationRepository.findByCustomerPhoneAndReference(phone, reference);
        if (reservation != null) {
            // Update the customer name
            reservation.getCustomer().setName(customerName);
            reservation.getCustomer().setPhone(customerPhone);
            reservation.setPax(Integer.parseInt(seatingCapacity));
            reservation.setPrefer(preferredSpace);
            reservationRepository.save(reservation); // save the reservation (cascades to customer if mapped)
            session.setAttribute("phone", customerPhone);

            WebUpdateDTO dto = new WebUpdateDTO();
            dto.setPax(reservation.getPax());
            dto.setCustomerName(customerName);
            dto.setCode("CHANGED_RESERVATION");
            dto.setReference(reservation.getReference());
            dto.setMessage("Customer "+dto.getCustomerName()+" has modified their reservation "+dto.getReference()+" details.");
            messagingTemplate.convertAndSend("/topic/forms",dto);


        }
        return "redirect:/queue"; // Redirect after successful update
    }

    @PostMapping("/cancelReservation")
    public String cancelReservation(HttpSession session) {
        String phone = (String) session.getAttribute("phone");
        String reference = (String) session.getAttribute("reference");

        if (phone == null || reference == null) {
            // session expired or user not logged in
            return "redirect:/loginpage";
        }

        Reservation reservation = reservationRepository.findByCustomerPhoneAndReference(phone, reference);
        if (reservation != null) {
            // Update the customer name
            reservation.setStatus("Cancelled");
            reservation.setReservationCancelledtime(LocalTime.now(ZoneOffset.UTC));
            reservationRepository.save(reservation);

            WebUpdateDTO dto = new WebUpdateDTO();

            dto.setCode("CANCELLED_RESERVATION");
            dto.setMessage(
                    "Reservation from " + reservation.getCustomer().getName()
                            + " (" + reservation.getPax() + " pax)"
                            + " | Ref: " + reservation.getReference()
                            + " has been cancelled"
            );
            dto.setPhone(reservation.getCustomer().getPhone());
            dto.setReference(reservation.getReference());
            dto.setPax(reservation.getPax());
            dto.setCustomerName(reservation.getCustomer().getName());

            String recipient = reservation.getCustomer().getPhone();
            String details = "Hello "+reservation.getCustomer().getName()+", your reservation "+reservation.getReference()+" has been cancelled.\n" +
                    "If you need assistance or wish to rebook, please contact us. Thank you.\n";

            smsService.sendSms(recipient,details);


            messagingTemplate.convertAndSend("/topic/forms", dto);

        }
        return "redirect:/queue"; // Redirect after successful update
    }
    @PostMapping("/confirmReservation")
    public String confirmReservation(HttpSession session) {
        String phone = (String) session.getAttribute("phone");
        String reference = (String) session.getAttribute("reference");

        if (phone == null || reference == null) {
            // session expired or user not logged in
            return "redirect:/loginpage";
        }

        Reservation reservation = reservationRepository.findByCustomerPhoneAndReference(phone, reference);
        if (reservation != null) {
            // Update the customer name
            reservation.setStatus("Confirm");
            reservation.setReservationConfirmtime(LocalTime.now(ZoneOffset.UTC));
            reservationRepository.save(reservation);

            List<Notification> pending = notificationRepository.findByAccountAndSentFalse(reference);
            for (Notification n : pending) {
                n.setSent(true);
                notificationRepository.save(n);
            }

            WebUpdateDTO dto = new WebUpdateDTO();

            dto.setCode("CONFIRM_RESERVATION");
            dto.setMessage(
                    "Reservation from " + reservation.getCustomer().getName()
                            + " (" + reservation.getPax() + " pax)"
                            + " | Ref: " + reservation.getReference()
                            + " has been confirmed"
            );

            dto.setPhone(reservation.getCustomer().getPhone());
            dto.setReference(reservation.getReference());
            dto.setPax(reservation.getPax());
            dto.setCustomerName(reservation.getCustomer().getName());

            messagingTemplate.convertAndSend("/topic/forms", dto);

        }
        return "redirect:/queue"; // Redirect after successful update
    }


    @GetMapping("/reservation/status")
    @ResponseBody
    public Map<String, String> getReservationStatus(
            @RequestParam Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow();

        return Map.of("status", reservation.getStatus());
    }


    @GetMapping("/pendingNotifications")
    public ResponseEntity<List<NotificationDTO>> getPendingNotifications(@RequestParam String reference) {
        try {
            List<Notification> pending = notificationRepository.findByAccountAndSentFalse(reference);

            // Convert to DTOs (safe JSON)
            List<NotificationDTO> dtoList = pending.stream()
                    .map(NotificationDTO::new)
                    .collect(Collectors.toList());

            // Mark as sent
            for (Notification n : pending) {
                n.setSent(false);
            }
            notificationRepository.saveAll(pending);

            return ResponseEntity.ok(dtoList);

        } catch (Exception e) {
            e.printStackTrace(); // server logs
            return ResponseEntity.status(500).build();
        }
    }


}
