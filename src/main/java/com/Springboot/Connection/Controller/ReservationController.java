package com.Springboot.Connection.Controller;

import com.Springboot.Connection.dto.CustomerReservationDTO;
import com.Springboot.Connection.dto.WebUpdateDTO;
import com.Springboot.Connection.model.Customer;
import com.Springboot.Connection.model.Reservation;
import com.Springboot.Connection.repository.CustomerRepository;
import com.Springboot.Connection.repository.ReservationRepository;
import com.Springboot.Connection.service.NotificationService;
import com.Springboot.Connection.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;


@Controller
public class ReservationController {
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    @Qualifier("reservationTaskExecutor")
    private TaskExecutor taskExecutor;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SmsService smsService;

    @Autowired
    private NotificationService notificationService;

    @RequestMapping("/")
    public String showForm() {

        return "Home";
    }

    @RequestMapping("/register")
    public String showRegistration() {
        return "Registration";
    }


    @RequestMapping("/loginpage")
    public String showLogin(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String newreservation, Model model){
        if (error != null) {
            model.addAttribute("errorMessage", error);
            model.addAttribute("status","error");
        }
        if(logout != null){
            model.addAttribute("errorMessage",logout);
            model.addAttribute("status","success");
        }
        if(newreservation != null){
            model.addAttribute("errorMessage", newreservation);
            model.addAttribute("status","success");
        }

        return  "Login";
    }



    @PostMapping("/reserve")
    public String saveReservation(CustomerReservationDTO reservationDTO) {

        // 1. Save customer and reservation (fast DB ops)
        Customer newCustomer = new Customer();
        newCustomer.setEmail(reservationDTO.getEmail());
        newCustomer.setName(reservationDTO.getName());
        newCustomer.setPhone(reservationDTO.getPhone());
        customerRepository.save(newCustomer);

        Reservation reservation = new Reservation();
        reservation.setPrefer(reservationDTO.getPrefer());
        reservation.setReference(String.format("RSV-%05d", reservationRepository.count() + 1));
        reservation.setPax(reservationDTO.getPax());
        reservation.setDate(LocalDate.now(ZoneOffset.UTC));
        reservation.setStatus("Pending");
        reservation.setReservationPendingtime(LocalTime.now(ZoneOffset.UTC));
        reservation.setCustomer(newCustomer);
        reservationRepository.save(reservation);

        // 2. Prepare DTO
        WebUpdateDTO dto = new WebUpdateDTO();
        dto.setCode("NEW_RESERVATION");
        dto.setMessage(
                "New reservation from " + reservationDTO.getName()
                        + " (" + reservationDTO.getPax() + " pax)"
                        + " | Ref: " + reservation.getReference()
                        + " has been added"
        );
        dto.setPhone(reservation.getCustomer().getPhone());
        dto.setReference(reservation.getReference());
        dto.setCustomerName(reservation.getCustomer().getName());
        dto.setPax(reservation.getPax());

        // 3. Run slow operations asynchronously
        CompletableFuture.runAsync(() -> {
            notificationService.notifyCustomer(newCustomer,reservation,dto);
        },taskExecutor);

        // 4. Return immediately
        return "redirect:/loginpage?newreservation=New Reservation Created Successfully";
    }




}
