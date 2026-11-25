package com.library.repository;

import com.library.model.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

public class InMemoryMemberRepository implements MemberRepository {
    private final Map<String, Member> storage = new ConcurrentSkipListMap<>();

    @Override
    public Member save(Member member) {
        storage.put(member.getMemberId(), member);
        return member;
    }

    @Override
    public Optional<Member> findById(String memberId) {
        return Optional.ofNullable(storage.get(memberId));
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean existsById(String memberId) {
        return storage.containsKey(memberId);
    }
}
