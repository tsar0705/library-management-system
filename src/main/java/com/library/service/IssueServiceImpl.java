package com.library.service;

import com.library.concurrent.KeyedLocks;
import com.library.exception.BookNotAvailableException;
import com.library.exception.BookReservedException;
import com.library.exception.LoanLimitExceededException;
import com.library.exception.NoActiveIssueException;
import com.library.model.Book;
import com.library.model.IssueRecord;
import com.library.model.Member;
import com.library.model.Reservation;
import com.library.policy.DailyRateFineCalculator;
import com.library.policy.FineCalculator;
import com.library.policy.LibraryPolicy;
import com.library.repository.BookRepository;
import com.library.repository.InMemoryReservationRepository;
import com.library.repository.IssueRecordRepository;
import com.library.repository.ReservationRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Locking: issue takes the member lock, then the book lock; return takes only the book lock.
 * A single global order (member, then book) means no two operations can wait on each other.
 */
public class IssueServiceImpl implements IssueService {
    private final LibraryCatalog libraryCatalog;
    private final MemberService memberService;
    private final BookRepository bookRepository;
    private final IssueRecordRepository issueRecordRepository;
    private final ReservationRepository reservationRepository;
    private final LibraryPolicy policy;
    private final FineCalculator fineCalculator;
    private final Clock clock;
    private final KeyedLocks bookLocks = new KeyedLocks();
    private final KeyedLocks memberLocks = new KeyedLocks();

    /** Convenience constructor: default policy, system clock, private reservation store. */
    public IssueServiceImpl(LibraryCatalog libraryCatalog, MemberService memberService,
                            BookRepository bookRepository, IssueRecordRepository issueRecordRepository) {
        this(libraryCatalog, memberService, bookRepository, issueRecordRepository,
                new InMemoryReservationRepository(), LibraryPolicy.DEFAULT,
                new DailyRateFineCalculator(LibraryPolicy.DEFAULT.getFinePerOverdueDay()), Clock.systemDefaultZone());
    }

    public IssueServiceImpl(LibraryCatalog libraryCatalog, MemberService memberService,
                            BookRepository bookRepository, IssueRecordRepository issueRecordRepository,
                            ReservationRepository reservationRepository, LibraryPolicy policy,
                            FineCalculator fineCalculator, Clock clock) {
        this.libraryCatalog = libraryCatalog;
        this.memberService = memberService;
        this.bookRepository = bookRepository;
        this.issueRecordRepository = issueRecordRepository;
        this.reservationRepository = reservationRepository;
        this.policy = policy;
        this.fineCalculator = fineCalculator;
        this.clock = clock;
    }

    @Override
    public IssueRecord issueBook(String bookId, String memberId) {
        Book book = libraryCatalog.getBook(bookId);          // BookNotFoundException
        Member member = memberService.getMember(memberId);   // MemberNotFoundException
        return memberLocks.withLock(memberId, () -> bookLocks.withLock(bookId, () -> doIssue(book, member)));
    }

    private IssueRecord doIssue(Book book, Member member) {
        if (!book.isAvailable()) {
            throw new BookNotAvailableException(book.getBookId());
        }
        Optional<Reservation> next = reservationRepository.peek(book.getBookId());
        if (next.isPresent() && !next.get().getMemberId().equals(member.getMemberId())) {
            throw new BookReservedException(book.getBookId());
        }
        if (issueRecordRepository.countActiveByMemberId(member.getMemberId()) >= policy.getMaxActiveLoansPerMember()) {
            throw new LoanLimitExceededException(member.getMemberId(), policy.getMaxActiveLoansPerMember());
        }
        next.ifPresent(r -> reservationRepository.poll(book.getBookId()));   // the head of the queue is being served

        book.markIssued();
        bookRepository.update(book);

        LocalDateTime now = LocalDateTime.now(clock);
        IssueRecord record = new IssueRecord(UUID.randomUUID().toString(), book.getBookId(),
                member.getMemberId(), now, now.plusDays(policy.getLoanPeriodDays()));
        return issueRecordRepository.save(record);
    }

    @Override
    public IssueRecord returnBook(String bookId) {
        Book book = libraryCatalog.getBook(bookId);
        return bookLocks.withLock(bookId, () -> {
            IssueRecord record = issueRecordRepository.findActiveByBookId(bookId)
                    .orElseThrow(() -> new NoActiveIssueException(bookId));
            LocalDateTime now = LocalDateTime.now(clock);
            record.markReturned(now, fineCalculator.calculateFine(record.getDueDate(), now));
            book.markAvailable();
            bookRepository.update(book);
            return record;
        });
    }

    @Override
    public List<IssueRecord> getIssueHistoryForMember(String memberId) {
        memberService.getMember(memberId);
        return issueRecordRepository.findByMemberId(memberId);
    }

    @Override
    public List<IssueRecord> getActiveLoansForMember(String memberId) {
        return getIssueHistoryForMember(memberId).stream().filter(r -> !r.isReturned()).collect(Collectors.toList());
    }

    @Override
    public List<IssueRecord> getAllIssueRecords() {
        return issueRecordRepository.findAll();
    }

    @Override
    public List<IssueRecord> getOverdueRecords() {
        LocalDateTime now = LocalDateTime.now(clock);
        return issueRecordRepository.findActive().stream().filter(r -> r.isOverdue(now)).collect(Collectors.toList());
    }

    @Override
    public BigDecimal getOutstandingFine(String bookId) {
        IssueRecord record = issueRecordRepository.findActiveByBookId(bookId)
                .orElseThrow(() -> new NoActiveIssueException(bookId));
        return fineCalculator.calculateFine(record.getDueDate(), LocalDateTime.now(clock));
    }
}
