package com.library.model;

import com.library.util.Validation;
import java.util.Objects;

public class Book {
    private final String bookId;
    private final String title;
    private final String author;
    private final String isbn;
    private volatile BookStatus status;

    public Book(String bookId, String title, String author, String isbn) {
        this.bookId = Validation.requireNonBlank(bookId, "bookId");
        this.title = Validation.requireNonBlank(title, "title");
        this.author = Validation.requireNonBlank(author, "author");
        this.isbn = Objects.requireNonNull(isbn, "isbn cannot be null");
        this.status = BookStatus.AVAILABLE;
    }

    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public BookStatus getStatus() { return status; }
    public boolean isAvailable() { return status == BookStatus.AVAILABLE; }

    public void markIssued() { this.status = BookStatus.ISSUED; }
    public void markAvailable() { this.status = BookStatus.AVAILABLE; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Book && bookId.equals(((Book) o).bookId));
    }

    @Override
    public int hashCode() { return bookId.hashCode(); }

    @Override
    public String toString() {
        return String.format("Book{id='%s', title='%s', author='%s', isbn='%s', status=%s}",
                bookId, title, author, isbn, status);
    }
}
