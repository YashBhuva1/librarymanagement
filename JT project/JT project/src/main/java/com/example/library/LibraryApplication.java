package com.example.library;

import com.example.library.book.Book;
import com.example.library.book.BookRepository;
import com.example.library.entity.Category;
import com.example.library.entity.User;
import com.example.library.repository.CategoryRepository;
import com.example.library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = {"com.example.library.entity", "com.example.library.book"})
@EnableJpaRepositories(basePackages = {"com.example.library.repository", "com.example.library.book"})
public class LibraryApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibraryApplication.class, args);
	}

	@Bean
	public CommandLineRunner dataInitializer(UserRepository userRepository, 
                                             CategoryRepository categoryRepository, 
                                             BookRepository bookRepository,
                                             PasswordEncoder passwordEncoder) {
		return args -> {
			if (categoryRepository.count() == 0) {
				Category fiction = categoryRepository.save(Category.builder().name("Fiction").build());
				Category science = categoryRepository.save(Category.builder().name("Science").build());
				Category tech = categoryRepository.save(Category.builder().name("Technology").build());

                if (bookRepository.count() == 0) {
                    bookRepository.save(Book.builder()
                            .title("The Great Gatsby")
                            .author("F. Scott Fitzgerald")
                            .category(fiction)
                            .quantity(5)
                            .availableCopies(5)
                            .build());
                    bookRepository.save(Book.builder()
                            .title("Clean Code")
                            .author("Robert C. Martin")
                            .category(tech)
                            .quantity(3)
                            .availableCopies(3)
                            .build());
                }
			}

			if (userRepository.findByEmail("admin@example.com").isEmpty()) {
				User admin = User.builder()
						.name("Admin User")
						.email("admin@example.com")
						.password(passwordEncoder.encode("admin123"))
						.role("ROLE_ADMIN")
						.build();
				userRepository.save(admin);
			}
		};
	}
}
