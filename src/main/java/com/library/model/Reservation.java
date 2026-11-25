package com.library.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** A member's place in the FIFO queue for a book that is currently out. */
public final class Reservation {
    private final String bookId;
    private final String memberId;
    private final LocalDateTime createdAt;

    public Reservation(String bookId, String memberId, LocalDateTime createdAt) {
        this.bookId = Objects.requireNonNull(bookId);
        this.memberId = Objects.requireNonNull(memberId);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public String getBookId() { return bookId; }
    public String getMemberId() { return memberId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return String.format("Reservation{bookId='%s', memberId='%s', createdAt=%s}", bookId, memberId, createdAt);
    }
}
