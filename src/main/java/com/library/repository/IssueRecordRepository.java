package com.library.repository;

import com.library.model.IssueRecord;
import java.util.List;
import java.util.Optional;

public interface IssueRecordRepository {
    IssueRecord save(IssueRecord record);
    Optional<IssueRecord> findActiveByBookId(String bookId);
    /** Records for one member, oldest first. */
    List<IssueRecord> findByMemberId(String memberId);
    /** Loans that have not been returned yet. */
    List<IssueRecord> findActive();
    long countActiveByMemberId(String memberId);
    /** All records, oldest first. */
    List<IssueRecord> findAll();
}
