package com.teamrhythm.concertwishlist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * @Bean
     * public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
     * http
     * .authorizeHttpRequests(authz -> authz
     * .requestMatchers("/", "/login", "/register", "/css/**", "/js/**",
     * "/images/**", "/error").permitAll()
     * .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll() // Allow
     * OAuth2 endpoints
     * .anyRequest().authenticated()
     * )
     * .oauth2Login(oauth2 -> oauth2
     * .loginPage("/login")
     * .defaultSuccessUrl("/artists", true) // Redirect here after Spotify login
     * .failureUrl("/login?error=true")
     * )
     * .logout(logout -> logout
     * .logoutUrl("/logout")
     * .logoutSuccessUrl("/")
     * .permitAll()
     * )
     * .csrf(csrf -> csrf.disable());
     * 
     * return http.build();
     * }
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll() // Allow everything for now
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/artists", true))
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}