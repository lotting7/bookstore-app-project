package com.bookstore.controller;

import com.bookstore.model.Book;
import com.bookstore.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Book Catalog", description = "Operations related to book catalog management")
public class BookController {

    private final BookService bookService;

    // pobieranie listy ksiazek (user/admin)
    @GetMapping
    @Operation(summary = "Get list of books", description = "Retrieve list of books with optional filtering by title, author, or status")
    public ResponseEntity<List<Book>> getBooks(
            @RequestParam(required = false) @Parameter(description = "Title of the book") String title,
            @RequestParam(required = false) @Parameter(description = "Author of the book") String author,
            @RequestParam(required = false) @Parameter(description = "Status of the book (available, reserved, borrowed)") String status) {
        
        List<Book> books = bookService.getBooks(title, author, status);
        return ResponseEntity.ok(books);
    }

    // dodawanie nowej ksiazki (admin)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add new book (ADMIN)", description = "Add a new book to the catalog with default status AVAILABLE")
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        Book savedBook = bookService.addBook(book);
        return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
    }

    // usuwanie ksiazki (admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete book by ID (ADMIN)", description = "Remove a book from the catalog by its ID")
    public ResponseEntity<Void> deleteBook(
            @PathVariable @Parameter(description = "ID of the book to delete") Long id) {
        
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
