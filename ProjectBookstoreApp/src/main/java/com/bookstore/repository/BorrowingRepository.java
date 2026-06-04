package com.bookstore.repository;

import com.bookstore.model.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    // pobieranie historii wypozyczen konkretnego uzytkownika
    List<Borrowing> findByUserId(Long userId);

    // pobieranie aktywnych wypozyczen/rezerwacji dla danej ksiazki
    List<Borrowing> findByBookIdAndStatusIn(Long bookId, List<String> statuses);
}
