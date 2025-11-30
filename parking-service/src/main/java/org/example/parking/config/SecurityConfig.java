package org.example.parking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
                        .requestMatchers(new AntPathRequestMatcher("/spots/**", HttpMethod.GET.name())).hasAuthority("SCOPE_read")
                        .requestMatchers(new AntPathRequestMatcher("/spots/**", HttpMethod.POST.name())).hasAuthority("SCOPE_write")
                        .requestMatchers(new AntPathRequestMatcher("/spots/**", HttpMethod.PUT.name())).hasAuthority("SCOPE_write")
                        .requestMatchers(new AntPathRequestMatcher("/spots/**", HttpMethod.PATCH.name())).hasAuthority("SCOPE_write")
                        .requestMatchers(new AntPathRequestMatcher("/spots/**", HttpMethod.DELETE.name())).hasAuthority("SCOPE_write")
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



