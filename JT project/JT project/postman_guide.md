# Library Management System - Postman Testing Guide

This guide describes how to test the API endpoints using Postman.

## Authentication (Global Setup)
The application uses **Session-based Authentication**.
- When you log in, the server sets a cookie (`JSESSIONID`).
- Postman automatically handles cookies via its "Cookie Jar". Ensure it is enabled.

---

## 1. Register a User
- **Method**: `POST`
- **URL**: `http://localhost:8080/users`
- **Body (JSON)**:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "ADMIN"
}
```

## 2. Login
- **Method**: `POST`
- **URL**: `http://localhost:8080/login`
- **Body (JSON)**:
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```
*After success, your session is active.*

## 3. Create a Category
- **Method**: `POST`
- **URL**: `http://localhost:8080/categories`
- **Body (JSON)**:
```json
{
  "name": "Science Fiction"
}
```

## 4. Add a Book
- **Method**: `POST`
- **URL**: `http://localhost:8080/books`
- **Body (JSON)**:
```json
{
  "title": "Dune",
  "author": "Frank Herbert",
  "categoryId": 1
}
```

## 5. Borrow a Book
- **Method**: `POST`
- **URL**: `http://localhost:8080/borrow`
- **Body (JSON)**:
```json
{
  "bookId": 1
}
```

## 6. Return a Book
- **Method**: `PUT`
- **URL**: `http://localhost:8080/borrow/return/1` (where `1` is the Borrow ID)
- OR
- **URL**: `http://localhost:8080/borrow/return/book/1` (where `1` is the Book ID)

---

## 7. View All Books
- **Method**: `GET`
- **URL**: `http://localhost:8080/books`

## 8. View All Categories
- **Method**: `GET`
- **URL**: `http://localhost:8080/categories`

## 9. Check Overdue Renewals
- **Method**: `GET`
- **URL**: `http://localhost:8080/renewals/overdue`
*Returns names of students whose 8-day borrow limit has exceeded.*
