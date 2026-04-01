package com.example.library.service;

import com.example.library.dto.UserRegistrationDto;
import com.example.library.entity.User;
import com.example.library.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        String role = dto.getRole() != null ? dto.getRole().toUpperCase() : "USER";
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(role)
                .build();

        return userRepository.save(user);
    }

    public java.util.List<User> getAllStudents() {
        java.util.List<User> students = userRepository.findByRole("ROLE_USER");
        students.addAll(userRepository.findByRole("USER"));
        return students;
    }

    public java.util.List<User> searchStudents(String query) {
        java.util.List<User> students = userRepository.searchStudents(query, "ROLE_USER");
        students.addAll(userRepository.searchStudents(query, "USER"));
        return students;
    }
}
