package com.library.service;

import com.library.model.Book;
import java.util.List;

public interface LibraryCatalog {
    Book addBook(String bookId, String title, String author, String isbn);
    Book getBook(String bookId);
    List<Book> getAllBooks();
    /** Case-insensitive "contains" search. */
    List<Book> searchByTitle(String titleFragment);
    /** Case-insensitive "contains" search. */
    List<Book> searchByAuthor(String authorFragment);
    List<Book> getAvailableBooks();
}
