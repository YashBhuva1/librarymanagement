package com.example.library.repository;

import com.example.library.entity.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    java.util.List<Borrow> findByBookId(Long bookId);
    java.util.List<Borrow> findByBookIdAndReturnDateIsNull(Long bookId);
    java.util.List<Borrow> findByUserIdAndBookIdAndReturnDateIsNull(Long userId, Long bookId);
}
