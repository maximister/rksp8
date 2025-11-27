package org.example.vehicle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности для Vehicle Service
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
                        .requestMatchers(HttpMethod.GET, "/vehicles/**").hasAuthority("SCOPE_read")
                        .requestMatchers(HttpMethod.POST, "/vehicles/**").hasAuthority("SCOPE_write")
                        .requestMatchers(HttpMethod.PUT, "/vehicles/**").hasAuthority("SCOPE_write")
                        .requestMatchers(HttpMethod.DELETE, "/vehicles/**").hasAuthority("SCOPE_write")
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



