package com.bookstore.repository;

import com.bookstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // szukanie uzytkownika po loginie (potrzebne do logowania)
    Optional<User> findByUsername(String username);

    // sprawdzenie czy login juz istnieje (potrzebne do rejestracji)
    boolean existsByUsername(String username);

    // sprawdzenie czy email juz istnieje (potrzebne do rejestracji)
    boolean existsByEmail(String email);
}
