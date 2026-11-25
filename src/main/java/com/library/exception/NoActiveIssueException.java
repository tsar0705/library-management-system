package com.library.exception;

public class NoActiveIssueException extends LibraryException {
    public NoActiveIssueException(String bookId) {
        super("No active issue record found for book id: " + bookId + ". Cannot return.");
    }
}
