package org.example.reservation.config;

import feign.RequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Конфигурация Feign для передачи JWT токена в межсервисных вызовах
 */
@Configuration
public class FeignConfig {

    private static final Logger log = LoggerFactory.getLogger(FeignConfig.class);

    /**
     * Interceptor для передачи JWT токена из текущего запроса
     * в исходящие Feign-запросы к другим микросервисам
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            log.debug("Feign Interceptor: Authentication = {}", authentication);
            
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                String token = jwtAuth.getToken().getTokenValue();
                requestTemplate.header("Authorization", "Bearer " + token);
                log.debug("Feign Interceptor: Added Bearer token (length: {})", token.length());
            } else {
                log.warn("Feign Interceptor: No JWT token found in SecurityContext! Auth type: {}", 
                    authentication != null ? authentication.getClass().getSimpleName() : "null");
            }
        };
    }
}
