package com.example.library.book;

import lombok.Data;

@Data
public class BookRequestDto {
    private String title;
    private String author;
    private Long categoryId;
}
