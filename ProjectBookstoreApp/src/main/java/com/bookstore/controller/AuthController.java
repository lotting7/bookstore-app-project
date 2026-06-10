package com.bookstore.controller;

import com.bookstore.dto.UserRegisterDto;
import com.bookstore.model.User;
import com.bookstore.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Operations", description = "Operations related to user authentication and registration")
public class AuthController {

    private final UserService userService;

    // endpoint rejestracji nowego uzytkownika (kazdy)
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Allows guests to register a new account with default ROLE_USER")
    public ResponseEntity<User> register(@Valid @RequestBody UserRegisterDto dto) {
        // wywolanie serwisu do zapisania uzytkownika w bazie
        User registeredUser = userService.registerUser(dto);
        
        // zwrocenie zapisanego uzytkownika i statusu 201 created
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }
}
