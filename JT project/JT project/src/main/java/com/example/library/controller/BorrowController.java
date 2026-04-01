package com.example.library.controller;

import com.example.library.dto.BorrowRequestDto;
import com.example.library.entity.Borrow;
import com.example.library.service.BorrowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/borrow")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping
    public ResponseEntity<?> borrowBook(@RequestBody BorrowRequestDto dto, Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }
            String userEmail = authentication.getName();
            Borrow borrow = borrowService.borrowBook(dto, userEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body("Book borrowed successfully. Borrow ID: " + borrow.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/return/{id}")
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        try {
            Borrow borrow = borrowService.returnBook(id);
            return ResponseEntity.ok("Book returned successfully on " + borrow.getReturnDate());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/return/book/{bookId}")
    public ResponseEntity<?> returnBookByBookId(@PathVariable Long bookId) {
        try {
            Borrow borrow = borrowService.returnBookByBookId(bookId);
            return ResponseEntity.ok("Book returned successfully on " + borrow.getReturnDate());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
