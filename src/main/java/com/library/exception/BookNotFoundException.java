package com.library.exception;

public class BookNotFoundException extends LibraryException {
    public BookNotFoundException(String bookId) {
        super("Book not found with id: " + bookId);
    }
}
