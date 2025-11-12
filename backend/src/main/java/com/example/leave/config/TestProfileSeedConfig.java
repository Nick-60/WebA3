package com.example.leave.config;

import com.example.leave.model.User;
import com.example.leave.model.UserRole;
import com.example.leave.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
@Profile("test")
public class TestProfileSeedConfig {

    @Bean
    CommandLineRunner seedTestUsers(UserRepository userRepository,
                                    PasswordEncoder encoder,
                                    JdbcTemplate jdbcTemplate) {
        return args -> {
            // Ensure departments exist (Flyway migration should insert, but be defensive)
            jdbcTemplate.update("INSERT INTO departments(name) SELECT 'Engineering' WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name='Engineering')");
            jdbcTemplate.update("INSERT INTO departments(name) SELECT 'HR' WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name='HR')");

            Long engineeringId = jdbcTemplate.queryForObject("SELECT id FROM departments WHERE name='Engineering'", Long.class);
            Long hrId = jdbcTemplate.queryForObject("SELECT id FROM departments WHERE name='HR'", Long.class);

            // Create emp if missing
            ensureUser(userRepository, encoder, "emp", "emp123", UserRole.EMPLOYEE, engineeringId, "emp@example.com");
            // Create mgr if missing
            ensureUser(userRepository, encoder, "mgr", "mgr123", UserRole.MANAGER, engineeringId, "mgr@example.com");
            // Create hr if missing
            ensureUser(userRepository, encoder, "hr", "hr123", UserRole.HR, hrId, "hr@example.com");

            // Update departments.manager_id
            Optional<User> mgr = userRepository.findByUsername("mgr");
            Optional<User> hr = userRepository.findByUsername("hr");
            mgr.ifPresent(m -> jdbcTemplate.update("UPDATE departments SET manager_id = ? WHERE name='Engineering'", m.getId()));
            hr.ifPresent(h -> jdbcTemplate.update("UPDATE departments SET manager_id = ? WHERE name='HR'", h.getId()));
        };
    }

    private void ensureUser(UserRepository userRepository,
                            PasswordEncoder encoder,
                            String username,
                            String rawPassword,
                            UserRole role,
                            Long departmentId,
                            String email) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User u = new User();
            u.setUsername(username);
            u.setPasswordHash(encoder.encode(rawPassword));
            u.setRole(role);
            u.setDepartmentId(departmentId);
            u.setEmail(email);
            userRepository.save(u);
        }
    }
}

