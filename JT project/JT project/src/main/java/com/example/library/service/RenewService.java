package com.example.library.service;

import com.example.library.entity.Borrow;
import com.example.library.entity.Renew;
import com.example.library.repository.RenewRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RenewService {

    private final RenewRepository renewRepository;

    public RenewService(RenewRepository renewRepository) {
        this.renewRepository = renewRepository;
    }

    @Transactional
    public void createRenewal(Borrow borrow) {
        if (borrow == null) {
            throw new RuntimeException("Borrow record must not be null");
        }
        Renew renew = Renew.builder()
                .borrow(borrow)
                .daysRemaining(8)
                .isFinished(false)
                .build();
        renewRepository.save(renew);
    }

    @Transactional
    public void finishRenewal(Long borrowId) {
        renewRepository.findByBorrowId(borrowId)
                .ifPresent(renew -> {
                    renew.setFinished(true);
                    renewRepository.save(renew);
                });
    }

    /**
     * Every 24 hours (86,400,000 milliseconds) decrease the remaining days.
     * In a production system, this could be a cron job like @Scheduled(cron = "0 0 0 * * *").
     */
    @Scheduled(fixedRate = 86400000)
    @Transactional
    public void decrementDaysPerDay() {
        List<Renew> activeRenewals = renewRepository.findByIsFinishedFalse();
        for (Renew renew : activeRenewals) {
            renew.setDaysRemaining(renew.getDaysRemaining() - 1);
            renewRepository.save(renew);
        }
    }

    public List<String> getOverdueStudentNames() {
        // time exceeds: daysRemaining <= 0
        return renewRepository.findByDaysRemainingLessThanEqualAndIsFinishedFalse(0)
                .stream()
                .map(renew -> renew.getBorrow().getUser().getName())
                .collect(Collectors.toList());
    }

    public List<Renew> getActiveRenewalsForUser(String email) {
        return renewRepository.findByBorrowUserEmailAndIsFinishedFalse(email);
    }

    @Transactional
    public void manualRenew(Long borrowId) {
        renewRepository.findByBorrowId(borrowId)
                .ifPresent(renew -> {
                    renew.setDaysRemaining(8);
                    renew.setFinished(false);
                    renewRepository.save(renew);
                });
    }
}
