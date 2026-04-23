package com.mycompany.reservationsystem.Controller;

import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.repository.UserRepository;
import com.mycompany.reservationsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PostMapping("/all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/get")
    public ResponseEntity<User> getUserById(@RequestBody IdRequest request) {
        return userRepository.findById(request.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/username")
    public ResponseEntity<User> getUserByUsername(@RequestBody UsernameRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    if (userDetails.getFirstname() != null) {
                        user.setFirstname(userDetails.getFirstname());
                    }
                    if (userDetails.getLastname() != null) {
                        user.setLastname(userDetails.getLastname());
                    }
                    if (userDetails.getUsername() != null) {
                        user.setUsername(userDetails.getUsername());
                    }
                    if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty() && !userDetails.getPassword().startsWith("$2")) {
                        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                    }
                    if (userDetails.getPosition() != null) {
                        user.setPosition(userDetails.getPosition());
                    }
                    if (userDetails.getStatus() != null) {
                        user.setStatus(userDetails.getStatus());
                    }
                    User updated = userRepository.save(user);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().<Void>build();
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().<Void>build();
        }
        userRepository.findById(id).ifPresent(user -> {
            user.setStatus(request.getStatus());
            userRepository.save(user);
        });
        return ResponseEntity.ok().<Void>build();
    }

    public static class IdRequest {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    public static class UsernameRequest {
        private String username;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    public static class StatusRequest {
        private String status;
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
