package com.library.exception;

public class BookNotAvailableException extends LibraryException {
    public BookNotAvailableException(String bookId) {
        super("Book with id " + bookId + " is currently issued and not available.");
    }
}
