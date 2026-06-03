package com.bookstore.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // klucz glowny

    @Column(nullable = false, unique = true)
    private String name; // nazwa roli, np. role_user, role_admin
}
