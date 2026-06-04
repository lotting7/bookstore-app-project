// glowny plik konfiguracji spring security. ustawia zasady dostepu do adresow oraz kodowanie bcrypt.
package com.bookstore.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // szyfrowanie hasla algorytmem bcrypt
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // wylaczenie csrf dla rest api
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // otwarty dostep do rejestracji i logowania
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll() // otwarty swagger do testow
                .anyRequest().authenticated() // reszta wymaga logowania
            )
            .httpBasic(Customizer.withDefaults()); // logowanie http basic
        
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager(); // ulatwia pobranie managera autoryzacji w kontrolerach
    }
}
