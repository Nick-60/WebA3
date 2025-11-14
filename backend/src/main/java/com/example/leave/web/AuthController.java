package com.example.leave.web;

import com.example.leave.model.User;
import com.example.leave.repository.UserRepository;
import com.example.leave.security.JwtService;
import com.example.leave.model.UserRole;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username, request.password));
        UserDetails principal = (UserDetails) auth.getPrincipal();
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        String token = jwtService.generateToken(principal, Map.of(
                "uid", user.getId(),
                "role", user.getRole().name()
        ));
        return ResponseEntity.ok(Map.of(
                "accessToken", token,
                "tokenType", "Bearer",
                "expiresIn", jwtService.getExpMinutes() * 60
        ));
    }

    public static class RegisterRequest {
        public String username;
        public String password;
        public String email;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req == null || req.username == null || req.username.isBlank() ||
            req.password == null || req.password.isBlank() ||
            req.email == null || req.email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "username/password/email 不能为空"));
        }
        if (req.username.length() > 50) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户名过长"));
        }
        if (req.password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "密码至少 6 位"));
        }
        if (req.email.length() > 120 || !req.email.contains("@")) {
            return ResponseEntity.badRequest().body(Map.of("message", "邮箱格式不正确或过长"));
        }

        if (userRepository.existsByUsername(req.username)) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户名已存在"));
        }
        if (userRepository.existsByEmail(req.email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "邮箱已被使用"));
        }

        User u = new User();
        u.setUsername(req.username);
        u.setPasswordHash(passwordEncoder.encode(req.password));
        u.setRole(UserRole.EMPLOYEE);
        u.setDepartmentId(null);
        u.setEmail(req.email);
        User saved = userRepository.save(u);

        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "username", saved.getUsername(),
                "email", saved.getEmail(),
                "role", saved.getRole().name()
        ));
    }
}
