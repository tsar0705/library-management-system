package com.library.repository;

import com.library.model.IssueRecord;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryIssueRecordRepository implements IssueRecordRepository {
    private static final Comparator<IssueRecord> OLDEST_FIRST =
            Comparator.comparing(IssueRecord::getIssueDate).thenComparing(IssueRecord::getRecordId);

    private final Map<String, IssueRecord> storage = new ConcurrentHashMap<>();

    @Override
    public IssueRecord save(IssueRecord record) {
        storage.put(record.getRecordId(), record);
        return record;
    }

    @Override
    public Optional<IssueRecord> findActiveByBookId(String bookId) {
        return storage.values().stream()
                .filter(r -> r.getBookId().equals(bookId) && !r.isReturned())
                .findFirst();
    }

    @Override
    public List<IssueRecord> findByMemberId(String memberId) {
        return storage.values().stream()
                .filter(r -> r.getMemberId().equals(memberId))
                .sorted(OLDEST_FIRST)
                .collect(Collectors.toList());
    }

    @Override
    public List<IssueRecord> findActive() {
        return storage.values().stream().filter(r -> !r.isReturned()).sorted(OLDEST_FIRST).collect(Collectors.toList());
    }

    @Override
    public long countActiveByMemberId(String memberId) {
        return storage.values().stream().filter(r -> r.getMemberId().equals(memberId) && !r.isReturned()).count();
    }

    @Override
    public List<IssueRecord> findAll() {
        return storage.values().stream().sorted(OLDEST_FIRST).collect(Collectors.toList());
    }
}
