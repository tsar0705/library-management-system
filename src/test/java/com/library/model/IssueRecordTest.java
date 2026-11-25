package com.library.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IssueRecordTest {
    private static final LocalDateTime ISSUED = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final LocalDateTime DUE = ISSUED.plusDays(14);

    private IssueRecord record() { return new IssueRecord("R1", "B1", "M1", ISSUED, DUE); }

    @Test @DisplayName("due date cannot precede the issue date")
    void dueBeforeIssueRejected() {
        assertThrows(IllegalArgumentException.class, () -> new IssueRecord("R1", "B1", "M1", ISSUED, ISSUED.minusSeconds(1)));
    }

    @Test @DisplayName("a loan is overdue only strictly after its due date")
    void overdueBoundary() {
        IssueRecord r = record();
        assertFalse(r.isOverdue(DUE.minusDays(1)));
        assertFalse(r.isOverdue(DUE));
        assertTrue(r.isOverdue(DUE.plusNanos(1)));
    }

    @Test @DisplayName("a returned loan is never overdue and keeps its fine")
    void returnedLoanNotOverdue() {
        IssueRecord r = record();
        r.markReturned(DUE.plusDays(3), new BigDecimal("15.00"));
        assertTrue(r.isReturned());
        assertFalse(r.isOverdue(DUE.plusDays(30)));
        assertEquals(new BigDecimal("15.00"), r.getFineCharged());
    }

    @Test @DisplayName("returning twice is an error")
    void doubleReturnRejected() {
        IssueRecord r = record();
        r.markReturned(DUE, BigDecimal.ZERO);
        assertThrows(IllegalStateException.class, () -> r.markReturned(DUE, BigDecimal.ZERO));
    }
}
