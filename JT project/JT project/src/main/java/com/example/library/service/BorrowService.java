package com.example.library.service;

import com.example.library.dto.BorrowRequestDto;
import com.example.library.book.Book;
import com.example.library.entity.Borrow;
import com.example.library.entity.User;
import com.example.library.book.BookRepository;
import com.example.library.repository.BorrowRepository;
import com.example.library.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class BorrowService {

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final RenewService renewService;

    public BorrowService(BorrowRepository borrowRepository, BookRepository bookRepository, UserRepository userRepository, RenewService renewService) {
        this.borrowRepository = borrowRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.renewService = renewService;
    }

    @Transactional
    public Borrow borrowBook(BorrowRequestDto dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getBookId() == null) {
            throw new RuntimeException("Book ID must not be null");
        }

        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (!borrowRepository.findByUserIdAndBookIdAndReturnDateIsNull(user.getId(), book.getId()).isEmpty()) {
            throw new RuntimeException("You have already borrowed a copy of this book. Please return it before borrowing another.");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No copies currently available for this book");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Borrow borrow = borrowRepository.save(Borrow.builder().borrowDate(LocalDate.now()).user(user).book(book).build());

        renewService.createRenewal(borrow);
        return borrow;
    }

    @Transactional
    public Borrow returnBook(Long borrowId) {
        if (borrowId == null) {
            throw new RuntimeException("Borrow ID must not be null");
        }

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Borrow record not found"));

        if (borrow.getReturnDate() != null) {
            throw new RuntimeException("Book already returned");
        }

        borrow.setReturnDate(LocalDate.now());

        Book book = borrow.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        renewService.finishRenewal(borrowId);
        return borrowRepository.save(borrow);
    }

    @Transactional
    public Borrow returnBookByBookId(Long bookId) {
        if (bookId == null) {
            throw new RuntimeException("Book ID must not be null");
        }

        java.util.List<Borrow> borrows = borrowRepository.findByBookId(bookId);
        Borrow activeBorrow = null;
        for (Borrow b : borrows) {
            if (b.getReturnDate() == null) {
                if (activeBorrow == null || b.getBorrowDate().isAfter(activeBorrow.getBorrowDate())) {
                    activeBorrow = b;
                }
            }
        }
        
        if (activeBorrow == null) {
            throw new RuntimeException("No active borrow record found for this book");
        }

        activeBorrow.setReturnDate(LocalDate.now());

        Book book = activeBorrow.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        renewService.finishRenewal(activeBorrow.getId());
        return borrowRepository.save(activeBorrow);
    }
}
