package com.example.library.book;

import com.example.library.entity.Category;
import com.example.library.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final com.example.library.repository.BorrowRepository borrowRepository;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository, com.example.library.repository.BorrowRepository borrowRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.borrowRepository = borrowRepository;
    }

    public Book addBook(BookRequestDto dto) {
        java.util.Optional<Book> existingBook = bookRepository.findByTitle(dto.getTitle());
        if (existingBook.isPresent()) {
            Book book = existingBook.get();
            book.setQuantity(book.getQuantity() + 1);
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            return bookRepository.save(book);
        }

        if (dto.getCategoryId() == null) {
            throw new RuntimeException("Category ID must not be null");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Book book = Book.builder()
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .category(category)
                .quantity(1)
                .availableCopies(1)
                .build();

        return bookRepository.save(book);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    public Book updateBook(Long id, BookRequestDto dto) {
        Book book = getBookById(id);
        
        // If categories changed, fetch new category
        if (dto.getCategoryId() == null) {
            throw new RuntimeException("Category ID must not be null");
        }
        
        if (!book.getCategory().getId().equals(dto.getCategoryId())) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            book.setCategory(category);
        }

        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        // Note: quantity and availableCopies are managed at add/borrow time
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        if (!borrowRepository.findByBookIdAndReturnDateIsNull(id).isEmpty()) {
            throw new RuntimeException("Cannot delete book: It is currently borrowed by a student.");
        }
        bookRepository.deleteById(id);
    }
}
