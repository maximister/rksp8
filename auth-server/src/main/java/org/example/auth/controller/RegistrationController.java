package org.example.auth.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.auth.entity.User;
import org.example.auth.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegistrationController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(new RegisterResponse(
                    user.getId(),
                    user.getUsername(),
                    "User registered successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
    }
    
    @Data
    @RequiredArgsConstructor
    public static class RegisterResponse {
        private final Long id;
        private final String username;
        private final String message;
    }
    
    @Data
    @RequiredArgsConstructor
    public static class ErrorResponse {
        private final String error;
    }
}

