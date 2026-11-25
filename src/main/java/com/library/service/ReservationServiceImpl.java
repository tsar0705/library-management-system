package com.library.service;

import com.library.exception.ReservationException;
import com.library.model.Book;
import com.library.model.Reservation;
import com.library.repository.IssueRecordRepository;
import com.library.repository.ReservationRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationServiceImpl implements ReservationService {
    private final LibraryCatalog libraryCatalog;
    private final MemberService memberService;
    private final IssueRecordRepository issueRecordRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public ReservationServiceImpl(LibraryCatalog libraryCatalog, MemberService memberService,
                                  IssueRecordRepository issueRecordRepository,
                                  ReservationRepository reservationRepository, Clock clock) {
        this.libraryCatalog = libraryCatalog;
        this.memberService = memberService;
        this.issueRecordRepository = issueRecordRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    @Override
    public Reservation reserveBook(String bookId, String memberId) {
        Book book = libraryCatalog.getBook(bookId);
        memberService.getMember(memberId);
        boolean holdsIt = issueRecordRepository.findActiveByBookId(bookId)
                .map(r -> r.getMemberId().equals(memberId)).orElse(false);
        if (holdsIt) {
            throw new ReservationException("Member " + memberId + " already has book " + bookId + " on loan.");
        }
        if (book.isAvailable() && reservationRepository.peek(bookId).isEmpty()) {
            throw new ReservationException("Book " + bookId + " is available; issue it instead of reserving.");
        }
        Reservation reservation = new Reservation(bookId, memberId, LocalDateTime.now(clock));
        if (!reservationRepository.enqueueIfAbsent(reservation)) {
            throw new ReservationException("Member " + memberId + " already has a reservation for book " + bookId + ".");
        }
        return reservation;
    }

    @Override
    public void cancelReservation(String bookId, String memberId) {
        if (!reservationRepository.remove(bookId, memberId)) {
            throw new ReservationException("No reservation by member " + memberId + " for book " + bookId + ".");
        }
    }

    @Override
    public List<Reservation> getQueueForBook(String bookId) {
        libraryCatalog.getBook(bookId);
        return reservationRepository.findByBookId(bookId);
    }
}
