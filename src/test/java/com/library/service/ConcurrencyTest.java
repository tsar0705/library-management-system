package com.library.service;

import static org.junit.jupiter.api.Assertions.*;

import com.library.exception.BookNotAvailableException;
import com.library.exception.LoanLimitExceededException;
import com.library.exception.ReservationException;
import com.library.model.Book;
import com.library.policy.LibraryPolicy;
import com.library.repository.BookRepository;
import com.library.repository.InMemoryBookRepository;
import com.library.testsupport.Concurrently;
import com.library.testsupport.TestLibrary;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Correctness under contention. All tests also fail the build if the services deadlock (30 s watchdog). */
class ConcurrencyTest {

    /** Persists slowly, widening the window between "check the member's loan count" and "record the loan". */
    private static final class SlowBookRepository implements BookRepository {
        private final BookRepository delegate = new InMemoryBookRepository();
        @Override public Book save(Book b) { return delegate.save(b); }
        @Override public Optional<Book> findById(String id) { return delegate.findById(id); }
        @Override public List<Book> findAll() { return delegate.findAll(); }
        @Override public List<Book> findByTitle(String t) { return delegate.findByTitle(t); }
        @Override public List<Book> findByAuthor(String a) { return delegate.findByAuthor(a); }
        @Override public boolean existsById(String id) { return delegate.existsById(id); }
        @Override public void update(Book b) {
            try { Thread.sleep(15); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            delegate.update(b);
        }
    }

    @Test @DisplayName("32 members race for one book: exactly one wins")
    void oneWinnerPerBook() {
        TestLibrary lib = new TestLibrary().withBooks(1).withMembers(32);
        List<Throwable> results = Concurrently.run(32, i -> lib.issues.issueBook("B1", "M" + (i + 1)));
        assertEquals(1, Concurrently.successes(results));
        for (Throwable t : results) assertTrue(t == null || t instanceof BookNotAvailableException, String.valueOf(t));
        assertEquals(1, lib.records.findActive().size());
    }

    @Test @DisplayName("one member racing for 6 different books never exceeds the loan limit of 3")
    void loanLimitHoldsUnderContention() {
        TestLibrary lib = new TestLibrary(LibraryPolicy.DEFAULT, new SlowBookRepository()).withBooks(6).withMembers(1);
        List<Throwable> results = Concurrently.run(6, i -> lib.issues.issueBook("B" + (i + 1), "M1"));
        assertEquals(3, Concurrently.successes(results), "successes");
        for (Throwable t : results) assertTrue(t == null || t instanceof LoanLimitExceededException, String.valueOf(t));
        assertEquals(3, lib.records.countActiveByMemberId("M1"));
    }

    @Test @DisplayName("issue/return cycles from many threads leave consistent state and do not deadlock")
    void issueReturnCycles() {
        TestLibrary lib = new TestLibrary().withBooks(3).withMembers(8);
        AtomicInteger issued = new AtomicInteger();
        AtomicInteger returned = new AtomicInteger();
        Concurrently.run(8, i -> {
            for (int n = 0; n < 60; n++) {
                String book = "B" + (n % 3 + 1);
                try {
                    lib.issues.issueBook(book, "M" + (i + 1));
                    issued.incrementAndGet();
                } catch (BookNotAvailableException | LoanLimitExceededException expected) {
                    continue;
                }
                lib.issues.returnBook(book);
                returned.incrementAndGet();
            }
        });
        assertTrue(issued.get() > 0);
        assertEquals(issued.get(), returned.get());
        assertTrue(lib.records.findActive().isEmpty());
        assertEquals(3, lib.catalog.getAvailableBooks().size());
        assertEquals(issued.get(), lib.issues.getAllIssueRecords().size());
    }

    @Test @DisplayName("the same member reserving concurrently is queued exactly once")
    void reservationsAreDeduplicated() {
        TestLibrary lib = new TestLibrary().withBooks(1).withMembers(2);
        lib.issues.issueBook("B1", "M1");
        List<Throwable> results = Concurrently.run(16, i -> lib.reservationService.reserveBook("B1", "M2"));
        assertEquals(1, Concurrently.successes(results));
        for (Throwable t : results) assertTrue(t == null || t instanceof ReservationException, String.valueOf(t));
        assertEquals(1, lib.reservationService.getQueueForBook("B1").size());
    }
}
