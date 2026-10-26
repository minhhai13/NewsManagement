package com.minhhai.newsmanagement.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/login", "/api/v1/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers("/api/v1/categories/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users/**", "/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/editorial/**").hasAnyRole("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/news/*/audit-logs").authenticated()
                        .requestMatchers("/api/v1/news/**").hasRole("CONTRIBUTOR")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setContentType("application/json;charset=UTF-8");
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.getWriter().write("{\"status\":401,\"detail\":\"Bạn chưa đăng nhập hoặc token không hợp lệ\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setContentType("application/json;charset=UTF-8");
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.getWriter().write("{\"status\":403,\"detail\":\"Bạn không có quyền thực hiện thao tác này\"}");
                        }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173"));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }
}
