package org.example.reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности для Reservation Service
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
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/reservations/**").hasAuthority("SCOPE_read")
                        .requestMatchers(HttpMethod.POST, "/reservations/**").hasAuthority("SCOPE_write")
                        .requestMatchers(HttpMethod.PUT, "/reservations/**").hasAuthority("SCOPE_write")
                        .requestMatchers(HttpMethod.PATCH, "/reservations/**").hasAuthority("SCOPE_write")
                        .requestMatchers(HttpMethod.DELETE, "/reservations/**").hasAuthority("SCOPE_write")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );
        
        return http.build();
    }
}



