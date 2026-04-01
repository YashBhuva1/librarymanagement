package com.example.library.dto;

import lombok.Data;

@Data
public class UserRegistrationDto {
    private String name;
    private String email;
    private String password;
    private String role; // e.g. "USER" or "ADMIN", will be prefixed with "ROLE_" internally
}
