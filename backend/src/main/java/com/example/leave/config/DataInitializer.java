package com.example.leave.config;

import com.example.leave.model.User;
import com.example.leave.model.UserRole;
import com.example.leave.repository.UserRepository;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Pattern SHA256_HEX = Pattern.compile("^[0-9a-fA-F]{64}$");

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            // Ensure emp001 exists with BCrypt(pass123)
            Optional<User> existing = userRepository.findByUsername("emp001");
            if (existing.isEmpty()) {
                User u = new User();
                u.setUsername("emp001");
                u.setEmail("emp001@example.com");
                u.setRole(UserRole.EMPLOYEE);
                u.setPasswordHash(encoder.encode("pass123"));
                userRepository.save(u);
            }

            // Convert seeded SHA2 users to BCrypt if detected
            userRepository.findByUsername("emp").ifPresent(u -> {
                if (SHA256_HEX.matcher(u.getPasswordHash()).matches()) {
                    u.setPasswordHash(encoder.encode("emp123"));
                    userRepository.save(u);
                }
            });
            userRepository.findByUsername("mgr").ifPresent(u -> {
                if (SHA256_HEX.matcher(u.getPasswordHash()).matches()) {
                    u.setPasswordHash(encoder.encode("mgr123"));
                    userRepository.save(u);
                }
            });
            userRepository.findByUsername("hr").ifPresent(u -> {
                if (SHA256_HEX.matcher(u.getPasswordHash()).matches()) {
                    u.setPasswordHash(encoder.encode("hr123"));
                    userRepository.save(u);
                }
            });
        };
    }
}

