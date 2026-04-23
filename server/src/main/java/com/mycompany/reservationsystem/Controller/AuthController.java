package com.mycompany.reservationsystem.Controller;

import com.mycompany.reservationsystem.dto.LoginRequest;
import com.mycompany.reservationsystem.dto.LoginResponse;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        
        if (user == null) {
            return ResponseEntity.ok(new LoginResponse(false, "User not found"));
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.ok(new LoginResponse(false, "Invalid password"));
        }
        
        String role = user.getPosition() != null ? user.getPosition().name() : "STAFF";
        
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        
        securityContextRepository.saveContext(context, httpRequest, null);

        HttpSession session = httpRequest.getSession(true);
        
        user.setStatus("Active");
        user.setLastActivity(java.time.LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok(new LoginResponse(true, "Login successful", 
                user.getId(), user.getUsername(), user.getFirstname(), user.getLastname(), role));
    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(HttpServletRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            userRepository.findByUsername(auth.getName()).ifPresent(user -> {
                user.setStatus("Offline");
                userRepository.save(user);
            });
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new LoginResponse(true, "Logout successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.ok(new LoginResponse(false, "Not authenticated"));
        }
        return ResponseEntity.ok(new LoginResponse(true, "Authenticated", null, 
                auth.getName(), null, null, auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")));
    }
}
