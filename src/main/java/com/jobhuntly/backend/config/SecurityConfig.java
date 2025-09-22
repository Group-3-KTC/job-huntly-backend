package com.jobhuntly.backend.config;

import com.jobhuntly.backend.security.AuthenticationFilter;
import com.jobhuntly.backend.security.WebEndpoints;
import com.jobhuntly.backend.security.handlers.RestAccessDeniedHandler;
import com.jobhuntly.backend.security.handlers.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfig corsConfig;

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    public RestAccessDeniedHandler restAccessDeniedHandler() {
        return new RestAccessDeniedHandler();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationFilter authenticationFilter
    ) throws Exception {

        http
                // CORS + CSRF
                .cors(c -> c.configurationSource(corsConfig.corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Xử lý lỗi 401/403 cho REST
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(restAuthenticationEntryPoint())
                        .accessDeniedHandler(restAccessDeniedHandler())
                )

                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/application").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/v1/report").authenticated()
                                .requestMatchers("/api/v1/auth/**", "/actuator/health").permitAll()
                                .requestMatchers(HttpMethod.GET, WebEndpoints.PUBLIC_GET).permitAll()
                                .requestMatchers(HttpMethod.POST, WebEndpoints.PUBLIC_POST).permitAll()
                                .requestMatchers(HttpMethod.PUT, WebEndpoints.PUBLIC_PUT).permitAll()
                                .requestMatchers(HttpMethod.DELETE, WebEndpoints.PUBLIC_DELETE).permitAll()
                                .requestMatchers("/ws/**").permitAll()
                                .requestMatchers("/api/v1/auth/me").authenticated()
                                .requestMatchers("/api/v1/save-job/**").authenticated()
                                .requestMatchers("/api/v1/follows/**").authenticated()
                                .requestMatchers("/api/v1/dev/ping-noti").authenticated()
                                .requestMatchers("/api/v1/interviews/**").authenticated()
                                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/actuator/health"
                                ).permitAll()
                                .anyRequest().authenticated()
                )

                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
