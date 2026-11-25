package com.library;

import com.library.exception.LibraryException;
import com.library.model.IssueRecord;
import com.library.policy.DailyRateFineCalculator;
import com.library.policy.FineCalculator;
import com.library.policy.LibraryPolicy;
import com.library.repository.*;
import com.library.service.*;
import java.time.Clock;
import java.time.Duration;

/** Console walkthrough of the library domain: issue/return, limits, reservations, fines, overdue reporting. */
public class Main {
    public static void main(String[] args) {
        BookRepository books = new InMemoryBookRepository();
        MemberRepository members = new InMemoryMemberRepository();
        IssueRecordRepository records = new InMemoryIssueRecordRepository();
        ReservationRepository reservations = new InMemoryReservationRepository();
        LibraryPolicy policy = new LibraryPolicy(14, 2, new java.math.BigDecimal("5.00"));
        FineCalculator fines = new DailyRateFineCalculator(policy.getFinePerOverdueDay());

        LibraryCatalog catalog = new LibraryCatalogImpl(books);
        MemberService memberService = new MemberServiceImpl(members);
        Clock now = Clock.systemDefaultZone();
        IssueService issues = new IssueServiceImpl(catalog, memberService, books, records, reservations, policy, fines, now);
        ReservationService reservationService = new ReservationServiceImpl(catalog, memberService, records, reservations, now);

        section("Catalog and members");
        catalog.addBook("B1", "Clean Code", "Robert C. Martin", "978-0132350884");
        catalog.addBook("B2", "Effective Java", "Joshua Bloch", "978-0134685991");
        catalog.addBook("B3", "Design Patterns", "Erich Gamma", "978-0201633610");
        memberService.registerMember("M1", "Asha", "asha@example.com");
        memberService.registerMember("M2", "Ravi", "ravi@example.com");
        System.out.println("Search 'java'      -> " + catalog.searchByTitle("java"));
        System.out.println("Search author 'bl' -> " + catalog.searchByAuthor("bl"));

        section("Issue and the loan limit (max " + policy.getMaxActiveLoansPerMember() + ")");
        IssueRecord r1 = issues.issueBook("B1", "M1");
        System.out.println("Issued: " + r1);
        issues.issueBook("B2", "M1");
        attempt("M1 borrows a 3rd book", () -> issues.issueBook("B3", "M1"));
        attempt("M2 borrows B1 (already out)", () -> issues.issueBook("B1", "M2"));

        section("Reservations (FIFO)");
        reservationService.reserveBook("B1", "M2");
        System.out.println("Queue for B1: " + reservationService.getQueueForBook("B1"));
        issues.returnBook("B1");
        attempt("M1 re-borrows B1 (M2 is first in the queue)", () -> issues.issueBook("B1", "M1"));
        System.out.println("M2 borrows B1: " + issues.issueBook("B1", "M2"));

        section("Late return and fines");
        Clock inThreeWeeks = Clock.offset(now, Duration.ofDays(21));
        IssueService later = new IssueServiceImpl(catalog, memberService, books, records, reservations, policy, fines, inThreeWeeks);
        System.out.println("Overdue 21 days from now: " + later.getOverdueRecords().size() + " loan(s)");
        System.out.println("Outstanding fine on B2: " + later.getOutstandingFine("B2"));
        System.out.println("Returned late: " + later.returnBook("B2"));
    }

    private static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }

    private static void attempt(String what, Runnable action) {
        try {
            action.run();
            System.out.println(what + " -> unexpectedly succeeded");
        } catch (LibraryException e) {
            System.out.println(what + " -> rejected: " + e.getMessage());
        }
    }
}
