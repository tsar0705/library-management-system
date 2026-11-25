package com.library.service;

import com.library.exception.BookNotFoundException;
import com.library.exception.DuplicateEntityException;
import com.library.model.Book;
import com.library.repository.BookRepository;

import java.util.List;
import java.util.stream.Collectors;

public class LibraryCatalogImpl implements LibraryCatalog {

    private final BookRepository bookRepository;

    public LibraryCatalogImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Book addBook(String bookId, String title, String author, String isbn) {
        if (bookRepository.existsById(bookId)) {
            throw new DuplicateEntityException("A book with id " + bookId + " already exists.");
        }
        Book book = new Book(bookId, title, author, isbn);
        return bookRepository.save(book);
    }

    @Override
    public Book getBook(String bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public List<Book> searchByTitle(String titleFragment) {
        return bookRepository.findByTitle(titleFragment);
    }

    @Override
    public List<Book> searchByAuthor(String authorFragment) {
        return bookRepository.findByAuthor(authorFragment);
    }

    @Override
    public List<Book> getAvailableBooks() {
        return bookRepository.findAll().stream()
                .filter(Book::isAvailable)
                .collect(Collectors.toList());
    }
}
