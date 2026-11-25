package com.library.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.library.model.Reservation;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryReservationRepositoryTest {
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 10, 0);
    private ReservationRepository repo;

    private Reservation res(String book, String member) { return new Reservation(book, member, T0); }

    @BeforeEach
    void setUp() { repo = new InMemoryReservationRepository(); }

    @Test @DisplayName("the queue is first-in, first-out")
    void fifo() {
        assertTrue(repo.enqueueIfAbsent(res("B1", "M1")));
        assertTrue(repo.enqueueIfAbsent(res("B1", "M2")));
        assertEquals("M1", repo.peek("B1").get().getMemberId());
        assertEquals("M1", repo.poll("B1").get().getMemberId());
        assertEquals("M2", repo.poll("B1").get().getMemberId());
        assertFalse(repo.poll("B1").isPresent());
    }

    @Test @DisplayName("a member cannot queue twice for the same book")
    void noDuplicates() {
        assertTrue(repo.enqueueIfAbsent(res("B1", "M1")));
        assertFalse(repo.enqueueIfAbsent(res("B1", "M1")));
        assertTrue(repo.enqueueIfAbsent(res("B2", "M1")));
        assertEquals(1, repo.findByBookId("B1").size());
    }

    @Test @DisplayName("removing from the middle keeps the order of the rest")
    void removal() {
        repo.enqueueIfAbsent(res("B1", "M1"));
        repo.enqueueIfAbsent(res("B1", "M2"));
        repo.enqueueIfAbsent(res("B1", "M3"));
        assertTrue(repo.remove("B1", "M2"));
        assertFalse(repo.remove("B1", "M2"));
        assertEquals("M3", repo.findByBookId("B1").get(1).getMemberId());
    }

    @Test @DisplayName("empty queues behave")
    void emptyQueue() {
        assertFalse(repo.peek("B9").isPresent());
        assertTrue(repo.findByBookId("B9").isEmpty());
        assertFalse(repo.remove("B9", "M1"));
    }
}
