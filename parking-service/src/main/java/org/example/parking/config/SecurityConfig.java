package org.example.parking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности для Parking Service
 * Сервис работает как OAuth2 Resource Server - проверяет JWT токены
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        // H2 Console доступна без авторизации (для отладки)
                        .requestMatchers("/h2-console/**").permitAll()
                        // Actuator endpoints
                        .requestMatchers("/actuator/**").permitAll()
                        // GET запросы доступны с scope "read"
                        .requestMatchers(HttpMethod.GET, "/spots/**").hasAuthority("SCOPE_read")
                        // POST, PUT, DELETE требуют scope "write"
                        .requestMatchers(HttpMethod.POST, "/spots/**").hasAuthority("SCOPE_write")
                        .requestMatchers(HttpMethod.PUT, "/spots/**").hasAuthority("SCOPE_write")
                        .requestMatchers(HttpMethod.PATCH, "/spots/**").hasAuthority("SCOPE_write")
                        .requestMatchers(HttpMethod.DELETE, "/spots/**").hasAuthority("SCOPE_write")
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )
                // Настройка Resource Server для проверки JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );
        
        return http.build();
    }
}



