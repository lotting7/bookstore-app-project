package com.bookstore.service;

import com.bookstore.dto.UserRegisterDto;
import com.bookstore.model.Role;
import com.bookstore.model.User;
import com.bookstore.repository.RoleRepository;
import com.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, roleRepository, passwordEncoder);
    }

    // test sprawdzajacy udana rejestracje uzytkownika i zaszyfrowanie hasla
    @Test
    @DisplayName("should register user successfully when login and email are free")
    void testRegisterUserSuccess() {
        UserRegisterDto dto = new UserRegisterDto("nowyUser", "noweHaslo", "email@test.com");
        Role userRole = Role.builder().name("ROLE_USER").build();
        User savedUser = User.builder().id(1L).username("nowyUser").password("hashedHaslo").email("email@test.com").roles(Collections.singleton(userRole)).build();

        when(userRepository.existsByUsername("nowyUser")).thenReturn(false);
        when(userRepository.existsByEmail("email@test.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("noweHaslo")).thenReturn("hashedHaslo");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerUser(dto);

        assertNotNull(result);
        assertEquals("nowyUser", result.getUsername());
        assertEquals("hashedHaslo", result.getPassword()); // haslo powinno byc zaszyfrowane
        verify(userRepository, times(1)).save(any(User.class));
    }

    // test sprawdzajacy ze proba rejestracji na zajety login wyrzuca blad 400
    @Test
    @DisplayName("should throw BAD_REQUEST when username is already taken")
    void testRegisterUserUsernameTaken() {
        UserRegisterDto dto = new UserRegisterDto("nowyUser", "noweHaslo", "email@test.com");

        when(userRepository.existsByUsername("nowyUser")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.registerUser(dto);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Username is already taken", exception.getReason());
        verify(userRepository, never()).save(any(User.class));
    }

    // test sprawdzajacy zmiane roli na admina
    @Test
    @DisplayName("should change user role successfully")
    void testChangeRoleSuccess() {
        Role userRole = Role.builder().name("ROLE_USER").build();
        Role adminRole = Role.builder().name("ROLE_ADMIN").build();
        // uzywamy zmiennego Set, poniewaz modyfikujemy go w kodzie serwisu (.clear() oraz .add())
        User user = User.builder().id(1L).roles(new HashSet<>(Collections.singleton(userRole))).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.changeRole(1L, "admin");

        assertNotNull(result);
        assertTrue(result.getRoles().contains(adminRole));
        assertFalse(result.getRoles().contains(userRole)); // stara rola powinna zostac usunieta
        verify(userRepository, times(1)).save(user);
    }

    // test sprawdzajacy bledy rejestracji (zajety email) oraz bledy zmiany roli
    @Test
    @DisplayName("should test registration and role change error scenarios")
    void testUserServiceErrors() {
        UserRegisterDto dto = new UserRegisterDto("user", "pass", "email@test.com");
        
        // 1. sprawdzanie bledu gdy rejestrujemy zajety email
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> userService.registerUser(dto));

        // 2. sprawdzanie bledu przy probie zmiany roli nieistniejacego uzytkownika
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> userService.changeRole(1L, "admin"));

        // 3. sprawdzanie bledu gdy podamy nieistniejaca nazwe roli (inna niz admin lub user)
        User user = User.builder().id(1L).roles(new HashSet<>()).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(ResponseStatusException.class, () -> userService.changeRole(1L, "invalid_role"));
    }
}
