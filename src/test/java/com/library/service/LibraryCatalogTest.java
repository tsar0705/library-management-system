package com.library.service;

import static org.junit.jupiter.api.Assertions.*;

import com.library.exception.BookNotFoundException;
import com.library.exception.DuplicateEntityException;
import com.library.testsupport.TestLibrary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LibraryCatalogTest {
    private final TestLibrary lib = new TestLibrary().withBooks(3);

    @Test @DisplayName("adding a book with an existing id is rejected")
    void duplicateBook() {
        assertThrows(DuplicateEntityException.class, () -> lib.catalog.addBook("B1", "Other", "Other", "x"));
        assertEquals(3, lib.catalog.getAllBooks().size());
    }

    @Test @DisplayName("an unknown book id raises BookNotFoundException")
    void unknownBook() {
        assertThrows(BookNotFoundException.class, () -> lib.catalog.getBook("B99"));
    }

    @Test @DisplayName("only available books are listed as available")
    void availableBooks() {
        lib.withMembers(1);
        lib.issues.issueBook("B2", "M1");
        assertEquals(2, lib.catalog.getAvailableBooks().size());
        lib.issues.returnBook("B2");
        assertEquals(3, lib.catalog.getAvailableBooks().size());
    }

    @Test @DisplayName("search finds partial, case-insensitive matches on title and author")
    void search() {
        assertEquals(1, lib.catalog.searchByTitle("TITLE 2").size());
        assertEquals(3, lib.catalog.searchByTitle("title").size());
        assertEquals(1, lib.catalog.searchByAuthor("author 3").size());
    }
}
