// glowny plik konfiguracji spring security. ustawia zasady dostepu do adresow oraz kodowanie bcrypt.
package com.bookstore.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@SecurityScheme(
    name = "basicAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "basic"
)
@OpenAPIDefinition(security = @SecurityRequirement(name = "basicAuth"))
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
                .requestMatchers("/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html").permitAll() // otwarty swagger do testow
                .requestMatchers(HttpMethod.GET, "/api/books").permitAll() // mozliwosc przegladania ksiazek nie bedac zalogowanym1
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

