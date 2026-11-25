package com.library.repository;

import com.library.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    Book save(Book book);
    Optional<Book> findById(String bookId);
    /** All books ordered by id. */
    List<Book> findAll();
    /** Case-insensitive "contains" match on the title. */
    List<Book> findByTitle(String titleFragment);
    /** Case-insensitive "contains" match on the author. */
    List<Book> findByAuthor(String authorFragment);
    boolean existsById(String bookId);
    void update(Book book);
}
