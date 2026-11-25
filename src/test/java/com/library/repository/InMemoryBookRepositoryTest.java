package com.library.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.library.model.Book;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryBookRepositoryTest {
    private BookRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryBookRepository();
        repo.save(new Book("B3", "Design Patterns", "Erich Gamma", "3"));
        repo.save(new Book("B1", "Clean Code", "Robert Martin", "1"));
        repo.save(new Book("B2", "Effective Java", "Joshua Bloch", "2"));
    }

    private List<String> ids(List<Book> books) { return books.stream().map(Book::getBookId).collect(Collectors.toList()); }

    @Test @DisplayName("findAll is ordered by id")
    void orderedById() {
        assertEquals(List.of("B1", "B2", "B3"), ids(repo.findAll()));
    }

    @Test @DisplayName("title and author search are case-insensitive 'contains'")
    void searching() {
        assertEquals(List.of("B2"), ids(repo.findByTitle("JAVA")));
        assertEquals(List.of("B1", "B2"), ids(repo.findByTitle("c")));   // "Clean Code", "Effective Java"; not "Design Patterns"
        assertEquals(List.of("B2"), ids(repo.findByAuthor("bloch")));
        assertTrue(repo.findByTitle("nothing like this").isEmpty());
    }

    @Test @DisplayName("lookup and existence checks")
    void lookup() {
        assertTrue(repo.existsById("B1"));
        assertFalse(repo.existsById("B9"));
        assertTrue(repo.findById("B2").isPresent());
        assertFalse(repo.findById("B9").isPresent());
    }

    @Test @DisplayName("update replaces the stored book")
    void update() {
        Book b = repo.findById("B1").get();
        b.markIssued();
        repo.update(b);
        assertFalse(repo.findById("B1").get().isAvailable());
    }
}
