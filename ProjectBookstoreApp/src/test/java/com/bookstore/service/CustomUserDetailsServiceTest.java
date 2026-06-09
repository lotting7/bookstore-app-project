package com.bookstore.service;

import com.bookstore.model.Role;
import com.bookstore.model.User;
import com.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    private UserRepository userRepository;
    private CustomUserDetailsService detailsService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        detailsService = new CustomUserDetailsService(userRepository);
    }

    // test sprawdzajacy poprawne zaladowanie uzytkownika do sesji spring security oraz przypadek bledu
    @Test
    @DisplayName("should load user details when user exists and throw exception when not found")
    void testLoadUserByUsername() {
        Role role = Role.builder().name("ROLE_USER").build();
        User user = User.builder()
                .username("user")
                .password("pass")
                .enabled(true)
                .roles(Collections.singleton(role))
                .build();

        // definiowanie ze uzytkownik istnieje, a 'notfound' nie istnieje
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());

        // sprawdzanie poprawnosci
        UserDetails result = detailsService.loadUserByUsername("user");
        assertNotNull(result);
        assertEquals("user", result.getUsername());

        // sprawdzanie rzucenia wyjatku gdy loguje sie nieistniejacy uzytkownik
        assertThrows(UsernameNotFoundException.class, () -> detailsService.loadUserByUsername("notfound"));
    }
}
