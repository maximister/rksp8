package org.example.reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/actuator/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/reservations/**", HttpMethod.GET.name())).hasAuthority("SCOPE_read")
                        .requestMatchers(new AntPathRequestMatcher("/reservations/**", HttpMethod.POST.name())).hasAuthority("SCOPE_write")
                        .requestMatchers(new AntPathRequestMatcher("/reservations/**", HttpMethod.PUT.name())).hasAuthority("SCOPE_write")
                        .requestMatchers(new AntPathRequestMatcher("/reservations/**", HttpMethod.PATCH.name())).hasAuthority("SCOPE_write")
                        .requestMatchers(new AntPathRequestMatcher("/reservations/**", HttpMethod.DELETE.name())).hasAuthority("SCOPE_write")
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



