package org.example.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Конфигурация CORS для Auth Server
 * Полностью разрешает все CORS-запросы (для разработки)
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Разрешаем ВСЕ источники
        configuration.addAllowedOrigin("*");
        
        // Разрешаем ВСЕ методы
        configuration.addAllowedMethod("*");
        
        // Разрешаем ВСЕ заголовки
        configuration.addAllowedHeader("*");
        
        // Отключаем credentials для совместимости с *
        configuration.setAllowCredentials(false);
        
        // Максимальное время кеширования
        configuration.setMaxAge(3600L);
        
        // Экспортируем все заголовки
        configuration.addExposedHeader("*");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }
}

