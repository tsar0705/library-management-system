package com.library.service;

import static org.junit.jupiter.api.Assertions.*;

import com.library.exception.*;
import com.library.model.IssueRecord;
import com.library.policy.LibraryPolicy;
import com.library.testsupport.TestLibrary;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IssueServiceTest {
    private final TestLibrary lib = new TestLibrary().withBooks(5).withMembers(2);

    @Test @DisplayName("issuing sets the due date from the policy and marks the book issued")
    void issueSetsDueDate() {
        IssueRecord r = lib.issues.issueBook("B1", "M1");
        assertEquals(r.getIssueDate().plusDays(14), r.getDueDate());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), r.getIssueDate());
        assertFalse(lib.catalog.getBook("B1").isAvailable());
        assertFalse(r.isReturned());
    }

    @Test @DisplayName("a book that is out cannot be issued again")
    void doubleIssue() {
        lib.issues.issueBook("B1", "M1");
        assertThrows(BookNotAvailableException.class, () -> lib.issues.issueBook("B1", "M2"));
        assertThrows(BookNotAvailableException.class, () -> lib.issues.issueBook("B1", "M1"));
    }

    @Test @DisplayName("unknown book or member is rejected before anything changes")
    void unknownEntities() {
        assertThrows(BookNotFoundException.class, () -> lib.issues.issueBook("B99", "M1"));
        assertThrows(MemberNotFoundException.class, () -> lib.issues.issueBook("B1", "M99"));
        assertTrue(lib.catalog.getBook("B1").isAvailable());
        assertTrue(lib.issues.getAllIssueRecords().isEmpty());
    }

    @Test @DisplayName("a member cannot exceed the loan limit, and a rejected issue leaves the book available")
    void loanLimit() {
        lib.issues.issueBook("B1", "M1");
        lib.issues.issueBook("B2", "M1");
        lib.issues.issueBook("B3", "M1");
        assertThrows(LoanLimitExceededException.class, () -> lib.issues.issueBook("B4", "M1"));
        assertTrue(lib.catalog.getBook("B4").isAvailable());
        lib.issues.issueBook("B4", "M2");   // the limit is per member
    }

    @Test @DisplayName("returning a book frees a loan slot")
    void returnFreesSlot() {
        lib.issues.issueBook("B1", "M1");
        lib.issues.issueBook("B2", "M1");
        lib.issues.issueBook("B3", "M1");
        lib.issues.returnBook("B2");
        assertEquals(2, lib.issues.getActiveLoansForMember("M1").size());
        lib.issues.issueBook("B4", "M1");
        assertEquals(3, lib.issues.getActiveLoansForMember("M1").size());
    }

    @Test @DisplayName("returning on time costs nothing")
    void onTimeReturn() {
        lib.issues.issueBook("B1", "M1");
        lib.clock.advance(Duration.ofDays(14));
        IssueRecord r = lib.issues.returnBook("B1");
        assertEquals(new BigDecimal("0.00"), r.getFineCharged());
        assertTrue(r.isReturned());
        assertTrue(lib.catalog.getBook("B1").isAvailable());
    }

    @Test @DisplayName("a late return is charged per started day")
    void lateReturn() {
        lib.issues.issueBook("B1", "M1");
        lib.clock.advance(Duration.ofDays(14).plusDays(3).plusHours(1));
        IssueRecord r = lib.issues.returnBook("B1");
        assertEquals(new BigDecimal("20.00"), r.getFineCharged());   // 4 started days x 5.00
    }

    @Test @DisplayName("custom policy: loan period, limit and fine rate are honoured")
    void customPolicy() {
        TestLibrary strict = new TestLibrary(new LibraryPolicy(7, 1, new BigDecimal("2.50"))).withBooks(2).withMembers(1);
        IssueRecord r = strict.issues.issueBook("B1", "M1");
        assertEquals(r.getIssueDate().plusDays(7), r.getDueDate());
        assertThrows(LoanLimitExceededException.class, () -> strict.issues.issueBook("B2", "M1"));
        strict.clock.advance(Duration.ofDays(9));
        assertEquals(new BigDecimal("5.00"), strict.issues.returnBook("B1").getFineCharged());   // 2 days x 2.50
    }

    @Test @DisplayName("returning a book that is not on loan is rejected, also the second time")
    void returnWithoutIssue() {
        assertThrows(NoActiveIssueException.class, () -> lib.issues.returnBook("B1"));
        lib.issues.issueBook("B1", "M1");
        lib.issues.returnBook("B1");
        assertThrows(NoActiveIssueException.class, () -> lib.issues.returnBook("B1"));
        assertThrows(BookNotFoundException.class, () -> lib.issues.returnBook("B99"));
    }

    @Test @DisplayName("overdue report lists only unreturned loans past their due date")
    void overdueReport() {
        lib.issues.issueBook("B1", "M1");
        lib.clock.advance(Duration.ofDays(10));
        lib.issues.issueBook("B2", "M2");
        assertTrue(lib.issues.getOverdueRecords().isEmpty());
        lib.clock.advance(Duration.ofDays(5));   // B1 is now 1 day late, B2 is not
        assertEquals(1, lib.issues.getOverdueRecords().size());
        assertEquals("B1", lib.issues.getOverdueRecords().get(0).getBookId());
        lib.issues.returnBook("B1");
        assertTrue(lib.issues.getOverdueRecords().isEmpty());
    }

    @Test @DisplayName("outstanding fine grows with time and is zero before the due date")
    void outstandingFine() {
        lib.issues.issueBook("B1", "M1");
        assertEquals(new BigDecimal("0.00"), lib.issues.getOutstandingFine("B1"));
        lib.clock.advance(Duration.ofDays(16));
        assertEquals(new BigDecimal("10.00"), lib.issues.getOutstandingFine("B1"));
        assertThrows(NoActiveIssueException.class, () -> lib.issues.getOutstandingFine("B2"));
    }

    @Test @DisplayName("history keeps returned loans; active loans do not")
    void history() {
        lib.issues.issueBook("B1", "M1");
        lib.clock.advance(Duration.ofHours(1));
        lib.issues.issueBook("B2", "M1");
        lib.issues.returnBook("B1");
        assertEquals(2, lib.issues.getIssueHistoryForMember("M1").size());
        assertEquals("B1", lib.issues.getIssueHistoryForMember("M1").get(0).getBookId());
        assertEquals(1, lib.issues.getActiveLoansForMember("M1").size());
        assertThrows(MemberNotFoundException.class, () -> lib.issues.getIssueHistoryForMember("M99"));
    }

    @Test @DisplayName("every domain error shares one base type")
    void commonBaseType() {
        assertThrows(LibraryException.class, () -> lib.issues.returnBook("B1"));
        assertThrows(LibraryException.class, () -> lib.catalog.getBook("nope"));
    }
}
