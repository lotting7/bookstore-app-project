// obiekt do przesylania danych rejestracji nowego uzytkownika. zawiera walidacje pol.
package com.bookstore.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterDto {

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username; // login uzytkownika

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password; // haslo uzytkownika

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email; // adres email
}
