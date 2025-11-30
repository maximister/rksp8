package org.example.auth.controller;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.auth.entity.User;
import org.example.auth.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());
        
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Пользователь с таким именем уже существует");
            return ResponseEntity.badRequest().body(error);
        }
        
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRoles(request.getRole() != null ? "ROLE_" + request.getRole() : "ROLE_USER");
        newUser.setEnabled(true);
        
        User savedUser = userRepository.save(newUser);
        log.info("User registered successfully: {}", savedUser.getUsername());
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUser.getId());
        response.put("username", savedUser.getUsername());
        response.put("roles", savedUser.getRoles());
        response.put("message", "Пользователь успешно зарегистрирован");
        
        return ResponseEntity.ok(response);
    }

    @Data
    static class RegisterRequest {
        private String username;
        private String password;
        private String role;
    }
}

