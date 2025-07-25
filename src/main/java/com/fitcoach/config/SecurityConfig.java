package com.fitcoach.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация Spring Security для FitCoach приложения
 * Разрешает доступ к статическим ресурсам и API endpoints
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Разрешаем доступ к статическим ресурсам (Mini App)
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/images/**").permitAll()
                
                // Разрешаем доступ к API endpoints
                .requestMatchers("/api/**").permitAll()
                
                // Разрешаем доступ к Swagger UI
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                
                // Разрешаем доступ к H2 Console (для разработки)
                .requestMatchers("/h2-console/**").permitAll()
                
                // Разрешаем доступ к актуатору
                .requestMatchers("/actuator/**").permitAll()
                
                // Для MVP разрешаем доступ ко всем остальным ресурсам
                .anyRequest().permitAll()
            )
            
            // Отключаем CSRF для API endpoints и разработки
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/h2-console/**")
            )
            
            // Настройки для H2 Console (разработка)
            .headers(headers -> headers
                .frameOptions().sameOrigin()
            );

        return http.build();
    }
} 