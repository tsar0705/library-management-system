package com.library.service;

import com.library.model.IssueRecord;
import java.math.BigDecimal;
import java.util.List;

public interface IssueService {
    /**
     * Issues a book. Fails if the book is out, reserved for someone else, or the member is at the loan limit.
     * Atomic per book and per member, so concurrent callers cannot double-issue a book or exceed the limit.
     */
    IssueRecord issueBook(String bookId, String memberId);

    /** Returns the book, charging a fine if it is late. */
    IssueRecord returnBook(String bookId);

    List<IssueRecord> getIssueHistoryForMember(String memberId);
    List<IssueRecord> getActiveLoansForMember(String memberId);
    List<IssueRecord> getAllIssueRecords();

    /** Loans that are still out and past their due date. */
    List<IssueRecord> getOverdueRecords();

    /** Fine that would be charged if the book were returned right now (zero if not late). */
    BigDecimal getOutstandingFine(String bookId);
}
