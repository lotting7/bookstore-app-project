package com.bookstore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrowings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // klucz glowny

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // kto wypozyczyl/zarezerwowal

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book; // jaka ksiazka

    @Column(name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate = LocalDateTime.now(); // data rozpoczecia

    @Column(name = "return_date")
    private LocalDateTime returnDate; // data zwrotu (puste na poczatku)

    @Column(nullable = false)
    private String status; // stan, np. reservation, active, returned
}
