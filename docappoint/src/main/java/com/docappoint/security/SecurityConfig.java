package com.docappoint.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtTokenProvider jwtTokenProvider
    ) throws Exception {

        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtTokenProvider);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/register", "/login")
                                .permitAll()

                                // Patient endpoints
                                .requestMatchers(
                                        "/appointments/book",
                                        "/appointments/me",
                                        "/appointments/*/cancel"
                                )
                                .hasAuthority("ROLE_PATIENT")

                                // Doctor endpoints
                                .requestMatchers(
                                        "/appointments/doctor",
                                        "/appointments/*/complete"
                                )
                                .hasAuthority("ROLE_DOCTOR")

                                .anyRequest()
                                .authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}