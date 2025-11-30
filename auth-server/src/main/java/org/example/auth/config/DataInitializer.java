package org.example.auth.config;

import lombok.RequiredArgsConstructor;
import org.example.auth.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserService userService;
    
    @Override
    public void run(String... args) {
        userService.initDefaultUsers();
    }
}
