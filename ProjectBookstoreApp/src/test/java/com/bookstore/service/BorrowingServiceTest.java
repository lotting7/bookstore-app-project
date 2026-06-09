package com.bookstore.service;

import com.bookstore.model.Book;
import com.bookstore.model.Borrowing;
import com.bookstore.model.Role;
import com.bookstore.model.User;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.BorrowingRepository;
import com.bookstore.service.strategy.AdminLimitValidator;
import com.bookstore.service.strategy.UserLimitValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BorrowingServiceTest {

    private BorrowingRepository borrowingRepository;
    private BookRepository bookRepository;
    private UserLimitValidator userLimitValidator;
    private AdminLimitValidator adminLimitValidator;
    private BorrowingService borrowingService;

    @BeforeEach
    void setUp() {
        borrowingRepository = mock(BorrowingRepository.class);
        bookRepository = mock(BookRepository.class);
        userLimitValidator = mock(UserLimitValidator.class);
        adminLimitValidator = mock(AdminLimitValidator.class);
        borrowingService = new BorrowingService(
                borrowingRepository,
                bookRepository,
                userLimitValidator,
                adminLimitValidator
        );
    }

    // test sprawdzajacy udana rezerwacje dostepnej ksiazki przez zwyklego uzytkownika
    @Test
    @DisplayName("should reserve book successfully when book is available")
    void testReserveBookSuccess() {
        User user = User.builder().id(1L).roles(Collections.singleton(Role.builder().name("ROLE_USER").build())).build();
        Book book = Book.builder().id(10L).title("Dziady").status("AVAILABLE").build();

        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        // programujemy zeby zapis w bazie zwracal obiekt wypozyczenia
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Borrowing result = borrowingService.reserveBook(10L, user);

        assertNotNull(result);
        assertEquals("RESERVATION", result.getStatus());
        assertEquals("RESERVED", book.getStatus()); // status ksiazki powinien sie zmienic na reserved
        
        // weryfikacja wywolania walidatora dla zwyklego uzytkownika
        verify(userLimitValidator, times(1)).checkLimits(user, borrowingRepository);
        verify(bookRepository, times(1)).save(book);
    }

    // test sprawdzajacy ze admin moze zwrocic ksiazke wypozyczona przez innego uzytkownika
    @Test
    @DisplayName("should allow admin to return book borrowed by any user")
    void testReturnBookByAdminSuccess() {
        User admin = User.builder().id(1L).roles(Collections.singleton(Role.builder().name("ROLE_ADMIN").build())).build();
        User reader = User.builder().id(2L).build();
        Book book = Book.builder().id(10L).status("BORROWED").build();
        Borrowing borrowing = Borrowing.builder().id(100L).user(reader).book(book).status("ACTIVE").build();

        when(borrowingRepository.findById(100L)).thenReturn(Optional.of(borrowing));
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Borrowing result = borrowingService.returnBook(100L, admin);

        assertNotNull(result);
        assertEquals("RETURNED", result.getStatus());
        assertEquals("AVAILABLE", book.getStatus()); // ksiazka powinna stac sie ponownie dostepna
        assertNotNull(result.getReturnDate());
    }

    // test sprawdzajacy ze zwykly uzytkownik dostanie blad 403 przy probie zwrotu cudzej ksiazki
    @Test
    @DisplayName("should throw FORBIDDEN when user tries to return another user's book")
    void testReturnBookForbidden() {
        User user1 = User.builder().id(2L).roles(Collections.singleton(Role.builder().name("ROLE_USER").build())).build();
        User user2 = User.builder().id(3L).build();
        Book book = Book.builder().id(10L).status("BORROWED").build();
        Borrowing borrowing = Borrowing.builder().id(100L).user(user2).book(book).status("ACTIVE").build();

        when(borrowingRepository.findById(100L)).thenReturn(Optional.of(borrowing));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            borrowingService.returnBook(100L, user1);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("You can only return your own borrowings", exception.getReason());

        verify(borrowingRepository, never()).save(any(Borrowing.class));
    }

    // test sprawdzajacy rozne scenariusze wypozyczania ksiazek oraz pobieranie historii
    @Test
    @DisplayName("should test borrowing scenarios and history")
    void testBorrowBookAndHistory() {
        User user = User.builder().id(1L).roles(Collections.singleton(Role.builder().name("ROLE_USER").build())).build();
        User admin = User.builder().id(2L).roles(Collections.singleton(Role.builder().name("ROLE_ADMIN").build())).build();
        
        Book book = Book.builder().id(10L).status("AVAILABLE").build();
        Book reservedBook = Book.builder().id(11L).status("RESERVED").build();
        Book borrowedBook = Book.builder().id(12L).status("BORROWED").build();
        
        Borrowing reservation = Borrowing.builder().user(user).book(reservedBook).status("RESERVATION").build();

        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(bookRepository.findById(11L)).thenReturn(Optional.of(reservedBook));
        when(bookRepository.findById(12L)).thenReturn(Optional.of(borrowedBook));
        when(borrowingRepository.findByBookIdAndStatusIn(anyLong(), any())).thenReturn(List.of(reservation));
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(i -> i.getArgument(0));

        //  available
        assertNotNull(borrowingService.borrowBook(10L, user));

        // rezerwacja przez tego samego uzytkownika
        assertNotNull(borrowingService.borrowBook(11L, user));

        // admin probuje odebrac rezerwacje innego uzytkownika
        assertThrows(ResponseStatusException.class, () -> borrowingService.borrowBook(11L, admin));

        // proba wypozyczenia ksiazki ktora jest juz wypozyczona
        assertThrows(ResponseStatusException.class, () -> borrowingService.borrowBook(12L, user));

        //  sprawdzanie pobierania historii dla zwyklego uzytkownika i admina
        assertNotNull(borrowingService.getHistory(user));
        assertNotNull(borrowingService.getHistory(admin));
    }
}
