package com.bookstore.controller;

import com.bookstore.model.Borrowing;
import com.bookstore.model.User;
import com.bookstore.repository.UserRepository;
import com.bookstore.service.BorrowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
@Tag(name = "Borrowing Operations", description = "Operations related to reserving, borrowing, and returning books")
public class BorrowingController {

    private final BorrowingService borrowingService;
    private final UserRepository userRepository;

    // rezerwowanie ksiazki o podanym id (user)
    @PostMapping("/reserve/{bookId}")
    @Operation(summary = "Reserve a book", description = "Allows an authenticated user to reserve a book by its ID")
    public ResponseEntity<Borrowing> reserveBook(
            @PathVariable @Parameter(description = "ID of the book to reserve") Long bookId, 
            Principal principal) {
        // pobranie zalogowanego uzytkownika z bazy
        User user = getUserByPrincipal(principal);
        // wywolanie serwisu
        Borrowing borrowing = borrowingService.reserveBook(bookId, user);
        return new ResponseEntity<>(borrowing, HttpStatus.CREATED);
    }

    // wypozyczanie ksiazki o podanym id (user)
    @PostMapping("/borrow/{bookId}")
    @Operation(summary = "Borrow a book", description = "Allows an authenticated user to borrow a book directly or convert an active reservation")
    public ResponseEntity<Borrowing> borrowBook(
            @PathVariable @Parameter(description = "ID of the book to borrow") Long bookId, 
            Principal principal) {

        User user = getUserByPrincipal(principal);

        Borrowing borrowing = borrowingService.borrowBook(bookId, user);
        return new ResponseEntity<>(borrowing, HttpStatus.CREATED);
    }

    // zwracanie ksiazki o podanym id wypozyczenia (user/admin)
    @PostMapping("/return/{borrowingId}")
    @Operation(summary = "Return a book (USER/ADMIN)", description = "Allows a user to return their borrowed book, or an admin to return a book for any user")
    public ResponseEntity<Borrowing> returnBook(
            @PathVariable @Parameter(description = "ID of the borrowing transaction") Long borrowingId, 
            Principal principal) {

        User user = getUserByPrincipal(principal);

        Borrowing borrowing = borrowingService.returnBook(borrowingId, user);
        return ResponseEntity.ok(borrowing);
    }

    // pobieranie historii wypozyczen zalogowanego uzytkownika (user/admin)
    @GetMapping("/history")
    @Operation(summary = "Get borrowing history (USER/ADMIN)", description = "Allows a user to view their own borrowing history, or an admin to view the entire bookstore history")
    public ResponseEntity<List<Borrowing>> getHistory(Principal principal) {

        User user = getUserByPrincipal(principal);

        List<Borrowing> history = borrowingService.getHistory(user);
        return ResponseEntity.ok(history);
    }

    // pomocnicza metoda do szybkiego pobierania zalogowanego uzytkownika po loginie
    private User getUserByPrincipal(Principal principal) {
        String username = principal.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }
}
