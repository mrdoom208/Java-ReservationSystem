package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.model.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class WebsiteSyncService {

    private final RestTemplate restTemplate;

    @Value("${website.url}")
    private String WebsiteUrl;
    private String websiteSettings = WebsiteUrl+"/settings/auto-cancel";

    @Autowired
    public WebsiteSyncService(RestTemplate restTemplate) {
         this.restTemplate = restTemplate;
    }

    public void sendCancellationPolicy(String policy) {
        String url = WebsiteUrl + "/settings/cancellation-policy?policy=" + URLEncoder.encode(policy, StandardCharsets.UTF_8);
        try {
            restTemplate.postForEntity(url, null, String.class);
        } catch (Exception e) {
            // Silent fail
        }
    }
    public void sendAutoDeleteMonths(int months) {
        String url = WebsiteUrl + "/settings/auto-delete?months=" + months;
        try {
            restTemplate.postForEntity(url, null, String.class);
        } catch (Exception e) {
            // Silent fail
        }
    }
    public String generateLoginLink(Reservation reservation) {
        String baseUrl = AppSettings.loadApplicationUrl(); // e.g., https://myrestaurant.com
        String phone = URLEncoder.encode(reservation.getCustomer().getPhone(), StandardCharsets.UTF_8);
        String reference = URLEncoder.encode(reservation.getReference(), StandardCharsets.UTF_8);

        return baseUrl + "/login?phone=" + phone + "&reference=" + reference;
    }
}
