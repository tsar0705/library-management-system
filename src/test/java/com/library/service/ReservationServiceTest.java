package com.library.service;

import static org.junit.jupiter.api.Assertions.*;

import com.library.exception.*;
import com.library.model.Reservation;
import com.library.policy.LibraryPolicy;
import com.library.testsupport.TestLibrary;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationServiceTest {
    private final TestLibrary lib = new TestLibrary().withBooks(3).withMembers(3);

    @Test @DisplayName("an available book cannot be reserved: issue it instead")
    void availableBook() {
        assertThrows(ReservationException.class, () -> lib.reservationService.reserveBook("B1", "M1"));
    }

    @Test @DisplayName("a book that is out can be reserved, once per member")
    void reserveIssuedBook() {
        lib.issues.issueBook("B1", "M1");
        Reservation r = lib.reservationService.reserveBook("B1", "M2");
        assertEquals("M2", r.getMemberId());
        assertThrows(ReservationException.class, () -> lib.reservationService.reserveBook("B1", "M2"));
    }

    @Test @DisplayName("the member holding a book cannot reserve it")
    void holderCannotReserve() {
        lib.issues.issueBook("B1", "M1");
        assertThrows(ReservationException.class, () -> lib.reservationService.reserveBook("B1", "M1"));
    }

    @Test @DisplayName("unknown book or member is rejected")
    void unknownEntities() {
        assertThrows(BookNotFoundException.class, () -> lib.reservationService.reserveBook("B99", "M1"));
        lib.issues.issueBook("B1", "M1");
        assertThrows(MemberNotFoundException.class, () -> lib.reservationService.reserveBook("B1", "M99"));
    }

    @Test @DisplayName("after a return, only the first member in the queue may borrow; the queue is then consumed")
    void queuePriority() {
        lib.issues.issueBook("B1", "M1");
        lib.reservationService.reserveBook("B1", "M2");
        lib.reservationService.reserveBook("B1", "M3");
        lib.issues.returnBook("B1");

        assertThrows(BookReservedException.class, () -> lib.issues.issueBook("B1", "M1"));   // the previous holder
        assertThrows(BookReservedException.class, () -> lib.issues.issueBook("B1", "M3"));   // second in line
        lib.issues.issueBook("B1", "M2");                                                    // first in line
        assertEquals(1, lib.reservationService.getQueueForBook("B1").size());
        assertEquals("M3", lib.reservationService.getQueueForBook("B1").get(0).getMemberId());
    }

    @Test @DisplayName("cancelling lets the next member through, and cancelling twice is an error")
    void cancel() {
        lib.issues.issueBook("B1", "M1");
        lib.reservationService.reserveBook("B1", "M2");
        lib.issues.returnBook("B1");
        lib.reservationService.cancelReservation("B1", "M2");
        lib.issues.issueBook("B1", "M3");   // queue is empty again
        assertThrows(ReservationException.class, () -> lib.reservationService.cancelReservation("B1", "M2"));
    }

    @Test @DisplayName("a front-of-queue member at the loan limit is rejected but keeps their place")
    void limitDoesNotConsumeReservation() {
        TestLibrary one = new TestLibrary(new LibraryPolicy(14, 1, BigDecimal.ONE)).withBooks(2).withMembers(2);
        one.issues.issueBook("B1", "M1");
        one.issues.issueBook("B2", "M2");
        one.reservationService.reserveBook("B1", "M2");       // M2 is at its limit (B2)
        one.issues.returnBook("B1");
        assertThrows(LoanLimitExceededException.class, () -> one.issues.issueBook("B1", "M2"));
        assertEquals(1, one.reservationService.getQueueForBook("B1").size());
        one.issues.returnBook("B2");
        one.issues.issueBook("B1", "M2");                     // now allowed, and the queue empties
        assertTrue(one.reservationService.getQueueForBook("B1").isEmpty());
    }

    @Test @DisplayName("a book that is available but has a queue can still be reserved (joins the line)")
    void joinExistingQueue() {
        lib.issues.issueBook("B1", "M1");
        lib.reservationService.reserveBook("B1", "M2");
        lib.issues.returnBook("B1");                          // available, M2 is waiting
        lib.reservationService.reserveBook("B1", "M3");
        assertEquals(2, lib.reservationService.getQueueForBook("B1").size());
    }
}
