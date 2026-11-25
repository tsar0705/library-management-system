package com.library.exception;

/** Base type of every domain-rule violation raised by the library services. */
public abstract class LibraryException extends RuntimeException {
    protected LibraryException(String message) {
        super(message);
    }
}
