package com.jobhuntly.backend.config;

import com.jobhuntly.backend.security.Endpoints;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class AppConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public GET
                        .requestMatchers(org.springframework.http.HttpMethod.GET, Endpoints.Public.GET).permitAll()
                        // Public POST
                        .requestMatchers(org.springframework.http.HttpMethod.POST, Endpoints.Public.POST).permitAll()
                        // Public PUT
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, Endpoints.Public.PUT).permitAll()
                        // Public DELETE
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, Endpoints.Public.DELETE).permitAll()
                        // Admin ALL
                        .requestMatchers(Endpoints.Admin.ALL).hasRole("ADMIN")
                        // Các request còn lại yêu cầu login
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}
