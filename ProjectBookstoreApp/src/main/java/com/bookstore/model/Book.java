package com.bookstore.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // klucz glowny

    @Column(nullable = false)
    private String title; // tytul ksiazki

    @Column(nullable = false)
    private String author; // autor ksiazki

    @Column(nullable = false, unique = true)
    private String isbn; // unikalny kod isbn

    @Column(nullable = false)
    private String status = "AVAILABLE"; // stan ksiazki, np. available, reserved, borrowed
}
