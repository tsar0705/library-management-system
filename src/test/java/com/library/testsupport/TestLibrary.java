package com.library.testsupport;

import com.library.policy.DailyRateFineCalculator;
import com.library.policy.LibraryPolicy;
import com.library.repository.*;
import com.library.service.*;
import java.time.Instant;

/** Fully wired library (in-memory repositories, controllable clock) for tests. */
public final class TestLibrary {
    public final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
    public final BookRepository books;
    public final MemberRepository members = new InMemoryMemberRepository();
    public final IssueRecordRepository records = new InMemoryIssueRecordRepository();
    public final ReservationRepository reservations = new InMemoryReservationRepository();
    public final LibraryPolicy policy;
    public final LibraryCatalog catalog;
    public final MemberService memberService;
    public final IssueService issues;
    public final ReservationService reservationService;

    public TestLibrary() { this(LibraryPolicy.DEFAULT, new InMemoryBookRepository()); }

    public TestLibrary(LibraryPolicy policy) { this(policy, new InMemoryBookRepository()); }

    public TestLibrary(LibraryPolicy policy, BookRepository books) {
        this.policy = policy;
        this.books = books;
        this.catalog = new LibraryCatalogImpl(books);
        this.memberService = new MemberServiceImpl(members);
        this.issues = new IssueServiceImpl(catalog, memberService, books, records, reservations, policy,
                new DailyRateFineCalculator(policy.getFinePerOverdueDay()), clock);
        this.reservationService = new ReservationServiceImpl(catalog, memberService, records, reservations, clock);
    }

    public TestLibrary withBooks(int count) {
        for (int i = 1; i <= count; i++) catalog.addBook("B" + i, "Title " + i, "Author " + i, "ISBN" + i);
        return this;
    }

    public TestLibrary withMembers(int count) {
        for (int i = 1; i <= count; i++) memberService.registerMember("M" + i, "Member " + i, "m" + i + "@example.com");
        return this;
    }
}
