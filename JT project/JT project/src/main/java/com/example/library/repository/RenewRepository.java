package com.example.library.repository;

import com.example.library.entity.Renew;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RenewRepository extends JpaRepository<Renew, Long> {

    List<Renew> findByIsFinishedFalse();

    List<Renew> findByDaysRemainingLessThanEqualAndIsFinishedFalse(int days);
    
    List<Renew> findByBorrowUserEmailAndIsFinishedFalse(String email);

    Optional<Renew> findByBorrowId(Long borrowId);
}
