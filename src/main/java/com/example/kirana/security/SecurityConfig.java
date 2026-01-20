package com.example.kirana.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableMethodSecurity
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RateLimitFilter rateLimitFilter;
    private final JwtFilter jwtFilter;

    public SecurityConfig(RateLimitFilter rateLimitFilter, JwtFilter jwtFilter) {
        this.rateLimitFilter = rateLimitFilter;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/store/**","/store").hasRole("SUPER_ADMIN")

                        .requestMatchers("/users/**","/users").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/products/**","/products").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/purchases/**","/purchases").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/report/**","/report").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        .requestMatchers("/transactions/**","/transactions").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


}
