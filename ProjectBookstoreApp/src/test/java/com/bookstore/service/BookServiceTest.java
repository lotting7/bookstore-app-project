package com.bookstore.service;

import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTest {

    private BookRepository bookRepository;
    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        bookService = new BookService(bookRepository);
    }

    // test sprawdzajacy dodawanie ksiazki z automatycznym ustawieniem statusu available
    @Test
    @DisplayName("should add book and set status to AVAILABLE")
    void testAddBook() {
        Book bookInput = Book.builder()
                .title("Dziady")
                .author("Adam Mickiewicz")
                .isbn("123456789")
                .build();

        Book bookSaved = Book.builder()
                .id(1L)
                .title("Dziady")
                .author("Adam Mickiewicz")
                .isbn("123456789")
                .status("AVAILABLE")
                .build();

        // definiowanie sztucznego zachowania zapisu w bazie
        when(bookRepository.save(bookInput)).thenReturn(bookSaved);

        Book result = bookService.addBook(bookInput);

        assertNotNull(result);
        assertEquals("AVAILABLE", result.getStatus());
        assertEquals(1L, result.getId());
        
        // weryfikacja czy wywolano metode zapisu w bazie
        verify(bookRepository, times(1)).save(bookInput);
    }

    // test sprawdzajacy czy usuniecie ksiazki przebiega pomyslnie, gdy ksiazka istnieje w bazie
    @Test
    @DisplayName("should delete book when book exists")
    void testDeleteBookSuccess() {
        Long bookId = 1L;

        // definiowanie ze ksiazka o podanym id istnieje
        when(bookRepository.existsById(bookId)).thenReturn(true);

        bookService.deleteBook(bookId);

        // weryfikacja czy usunieto ksiazke o odpowiednim id
        verify(bookRepository, times(1)).deleteById(bookId);
    }

    // test sprawdzajacy czy proba usuniecia nieistniejacej ksiazki rzuca blad 404
    @Test
    @DisplayName("should throw NOT_FOUND when book to delete does not exist")
    void testDeleteBookNotFound() {
        Long bookId = 1L;

        when(bookRepository.existsById(bookId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            bookService.deleteBook(bookId);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Book not found", exception.getReason());

        verify(bookRepository, never()).deleteById(bookId);
    }

    // test sprawdzajacy wszystkie cztery opcje filtrowania i pobierania ksiazek z bazy
    @Test
    @DisplayName("should test all book filtering options")
    void testGetBooks() {

        // rozne kombinacje
        when(bookRepository.findByTitleContainingIgnoreCase("title")).thenReturn(List.of());
        when(bookRepository.findByAuthorContainingIgnoreCase("author")).thenReturn(List.of());
        when(bookRepository.findByStatus("AVAILABLE")).thenReturn(List.of());
        when(bookRepository.findAll()).thenReturn(List.of());

        // rozne parametry
        assertNotNull(bookService.getBooks("title", null, null));
        assertNotNull(bookService.getBooks(null, "author", null));
        assertNotNull(bookService.getBooks(null, null, "AVAILABLE"));
        assertNotNull(bookService.getBooks(null, null, null));
    }
}
