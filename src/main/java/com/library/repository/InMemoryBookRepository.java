package com.library.repository;

import com.library.model.Book;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

/** Thread-safe, id-ordered in-memory store. Swap for a JDBC/JPA implementation without touching services. */
public class InMemoryBookRepository implements BookRepository {
    private final Map<String, Book> storage = new ConcurrentSkipListMap<>();

    @Override
    public Book save(Book book) {
        storage.put(book.getBookId(), book);
        return book;
    }

    @Override
    public Optional<Book> findById(String bookId) {
        return Optional.ofNullable(storage.get(bookId));
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Book> findByTitle(String titleFragment) {
        String needle = titleFragment.toLowerCase(Locale.ROOT);
        return storage.values().stream()
                .filter(b -> b.getTitle().toLowerCase(Locale.ROOT).contains(needle))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> findByAuthor(String authorFragment) {
        String needle = authorFragment.toLowerCase(Locale.ROOT);
        return storage.values().stream()
                .filter(b -> b.getAuthor().toLowerCase(Locale.ROOT).contains(needle))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String bookId) {
        return storage.containsKey(bookId);
    }

    @Override
    public void update(Book book) {
        storage.put(book.getBookId(), book);
    }
}
