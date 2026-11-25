package com.library.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.library.model.IssueRecord;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryIssueRecordRepositoryTest {
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 10, 0);
    private IssueRecordRepository repo;

    private IssueRecord rec(String id, String book, String member, int dayOffset) {
        return new IssueRecord(id, book, member, T0.plusDays(dayOffset), T0.plusDays(dayOffset + 14));
    }

    @BeforeEach
    void setUp() {
        repo = new InMemoryIssueRecordRepository();
        repo.save(rec("R2", "B2", "M1", 1));
        repo.save(rec("R1", "B1", "M1", 0));
        repo.save(rec("R3", "B3", "M2", 2));
    }

    @Test @DisplayName("records come back oldest first")
    void ordering() {
        assertEquals("R1", repo.findAll().get(0).getRecordId());
        assertEquals("R2", repo.findByMemberId("M1").get(1).getRecordId());
    }

    @Test @DisplayName("active lookups ignore returned loans")
    void activeLookups() {
        assertEquals(2, repo.countActiveByMemberId("M1"));
        repo.findActiveByBookId("B1").get().markReturned(T0.plusDays(3), BigDecimal.ZERO);
        assertEquals(1, repo.countActiveByMemberId("M1"));
        assertFalse(repo.findActiveByBookId("B1").isPresent());
        assertEquals(2, repo.findActive().size());
    }

    @Test @DisplayName("an unknown book or member has no active loans")
    void unknownKeys() {
        assertFalse(repo.findActiveByBookId("B9").isPresent());
        assertEquals(0, repo.countActiveByMemberId("M9"));
        assertTrue(repo.findByMemberId("M9").isEmpty());
    }
}
