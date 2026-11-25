package com.library.exception;

public class BookReservedException extends LibraryException {
    public BookReservedException(String bookId) {
        super("Book " + bookId + " is reserved for another member who is first in the queue.");
    }
}
