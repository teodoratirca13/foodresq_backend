package com.foodresq.config;

import com.foodresq.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs",
                "/v3/api-docs/**"
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/listings", "/api/listings/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/listings").hasRole("BUSINESS")
                        .requestMatchers(HttpMethod.POST, "/api/listings/*/reserve").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/listings/*/claim").hasRole("ONG")
                        .requestMatchers(HttpMethod.POST, "/api/listings/*/complete").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/listings/*/cancel").authenticated()

                        .requestMatchers(HttpMethod.DELETE, "/api/listings/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/cart").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/cart/items").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/cart/items/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/cart/checkout").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/orders/my").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/cart").hasRole("USER")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
