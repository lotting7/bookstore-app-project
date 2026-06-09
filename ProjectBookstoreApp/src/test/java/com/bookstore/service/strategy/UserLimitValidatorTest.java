package com.bookstore.service.strategy;

import com.bookstore.model.Borrowing;
import com.bookstore.model.User;
import com.bookstore.repository.BorrowingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserLimitValidatorTest {

    private BorrowingRepository borrowingRepository;
    private UserLimitValidator userLimitValidator;

    @BeforeEach
    void setUp() {
        borrowingRepository = mock(BorrowingRepository.class);
        userLimitValidator = new UserLimitValidator();
    }

    // test sprawdzajacy ze walidacja przechodzi pomyslnie gdy uzytkownik ma mniej niz 5 aktywnych ksiazek
    @Test
    @DisplayName("should pass validation when user has less than 5 active books")
    void testCheckLimitsSuccess() {
        User user = User.builder().id(1L).build();
        List<Borrowing> activeBorrowings = new ArrayList<>();
        // 3 aktywne wypozyczenia
        for (int i = 0; i < 3; i++) {
            activeBorrowings.add(Borrowing.builder().status("ACTIVE").build());
        }

        // zwracanie wypozyczenia dla uzytkownika
        when(borrowingRepository.findByUserId(user.getId())).thenReturn(activeBorrowings);

        assertDoesNotThrow(() -> userLimitValidator.checkLimits(user, borrowingRepository));
    }

    // test sprawdzajacy ze walidacja rzuca blad bad_request gdy uzytkownik osiągnie limit 5 aktywnych ksiazek
    @Test
    @DisplayName("should throw BAD_REQUEST when user has 5 or more active books")
    void testCheckLimitsExceeded() {
        User user = User.builder().id(1L).build();
        List<Borrowing> activeBorrowings = new ArrayList<>();
        // 5 aktywnych wypozyczen
        for (int i = 0; i < 5; i++) {
            activeBorrowings.add(Borrowing.builder().status("ACTIVE").build());
        }

        // mock zwraca 5 aktywnych wypozyczen
        when(borrowingRepository.findByUserId(user.getId())).thenReturn(activeBorrowings);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userLimitValidator.checkLimits(user, borrowingRepository);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Reached limit of 5 active books", exception.getReason());
    }
}
