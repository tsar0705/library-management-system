package com.library.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BookTest {
    @Test @DisplayName("a new book is available")
    void newBookIsAvailable() {
        Book b = new Book("B1", "Clean Code", "Martin", "123");
        assertTrue(b.isAvailable());
        assertEquals(BookStatus.AVAILABLE, b.getStatus());
    }

    @Test @DisplayName("status flips between issued and available")
    void statusTransitions() {
        Book b = new Book("B1", "Clean Code", "Martin", "123");
        b.markIssued();
        assertFalse(b.isAvailable());
        b.markAvailable();
        assertTrue(b.isAvailable());
    }

    @Test @DisplayName("blank id, title or author is rejected")
    void rejectsBlankFields() {
        assertThrows(IllegalArgumentException.class, () -> new Book(" ", "T", "A", "I"));
        assertThrows(IllegalArgumentException.class, () -> new Book("B1", "", "A", "I"));
        assertThrows(IllegalArgumentException.class, () -> new Book("B1", "T", null, "I"));
        assertThrows(NullPointerException.class, () -> new Book("B1", "T", "A", null));
    }

    @Test @DisplayName("books are equal when their ids are equal")
    void equalityIsById() {
        assertEquals(new Book("B1", "T", "A", "I"), new Book("B1", "Other", "Other", "X"));
        assertNotEquals(new Book("B1", "T", "A", "I"), new Book("B2", "T", "A", "I"));
    }
}
