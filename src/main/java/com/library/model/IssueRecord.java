package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/** One loan of one book to one member, from issue to (optional) return. */
public class IssueRecord {
    private final String recordId;
    private final String bookId;
    private final String memberId;
    private final LocalDateTime issueDate;
    private final LocalDateTime dueDate;
    private volatile LocalDateTime returnDate;
    private volatile BigDecimal fineCharged = BigDecimal.ZERO;

    public IssueRecord(String recordId, String bookId, String memberId, LocalDateTime issueDate, LocalDateTime dueDate) {
        this.recordId = Objects.requireNonNull(recordId);
        this.bookId = Objects.requireNonNull(bookId);
        this.memberId = Objects.requireNonNull(memberId);
        this.issueDate = Objects.requireNonNull(issueDate);
        this.dueDate = Objects.requireNonNull(dueDate);
        if (dueDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("dueDate cannot be before issueDate");
        }
    }

    public String getRecordId() { return recordId; }
    public String getBookId() { return bookId; }
    public String getMemberId() { return memberId; }
    public LocalDateTime getIssueDate() { return issueDate; }
    public LocalDateTime getDueDate() { return dueDate; }
    public LocalDateTime getReturnDate() { return returnDate; }
    public BigDecimal getFineCharged() { return fineCharged; }
    public boolean isReturned() { return returnDate != null; }

    /** A loan is overdue while it is still out and the due date has passed. */
    public boolean isOverdue(LocalDateTime now) {
        return !isReturned() && now.isAfter(dueDate);
    }

    public void markReturned(LocalDateTime returnDate, BigDecimal fine) {
        if (isReturned()) {
            throw new IllegalStateException("Record " + recordId + " is already returned");
        }
        this.fineCharged = Objects.requireNonNull(fine);
        this.returnDate = Objects.requireNonNull(returnDate);
    }

    @Override
    public String toString() {
        return String.format("IssueRecord{id='%s', bookId='%s', memberId='%s', issued=%s, due=%s, returned=%s, fine=%s}",
                recordId, bookId, memberId, issueDate, dueDate, returnDate, fineCharged);
    }
}
