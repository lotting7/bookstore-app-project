package com.bookstore.controller;

import com.bookstore.model.User;
import com.bookstore.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Operations", description = "Operations related to user account management")
public class UserController {

    private final UserService userService;

    // zmiana roli uzytkownika o podanym id (admin)
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change user role (ADMIN)", description = "Allows an admin to change the role of a user by their ID")
    public ResponseEntity<User> changeRole(
            @PathVariable @Parameter(description = "ID of the user to update") Long id, 
            @RequestParam @Parameter(description = "Target role name ('admin' or 'user')") String roleName) {
        
        User updatedUser = userService.changeRole(id, roleName);
        return ResponseEntity.ok(updatedUser);
    }
}
