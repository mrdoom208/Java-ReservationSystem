package com.mycompany.reservationsystem.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/ws").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/raw-ws").permitAll()
                .requestMatchers("/raw-ws/**").permitAll()
                .requestMatchers("/topic/**").permitAll()
                .requestMatchers("/app/**").permitAll()
                .requestMatchers("/").permitAll()
                .requestMatchers("/loginpage").permitAll()
                .requestMatchers("/register").permitAll()
                .requestMatchers("/css/**").permitAll()
                .requestMatchers("/js/**").permitAll()
                .requestMatchers("/img/**").permitAll()
                .requestMatchers("/api/reservation/**").hasAnyRole("ADMINISTRATOR", "MANAGER", "STAFF")
                .requestMatchers("/api/table/**").hasAnyRole("ADMINISTRATOR", "MANAGER")
                .requestMatchers("/api/user/**").hasAnyRole("ADMINISTRATOR", "MANAGER")
                .requestMatchers("/api/settings/**").hasAnyRole("ADMINISTRATOR")
                .requestMatchers("/api/activity-logs/**").hasAnyRole("ADMINISTRATOR", "MANAGER")
                .requestMatchers("/api/message/**").hasAnyRole("ADMINISTRATOR", "MANAGER")
                .requestMatchers("/api/report/**").hasAnyRole("ADMINISTRATOR", "MANAGER")
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .securityContext(context -> context
                .securityContextRepository(securityContextRepository())
            );
        
        return http.build();
    }
}
