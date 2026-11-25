package com.library.service;

import com.library.exception.DuplicateEntityException;
import com.library.exception.MemberNotFoundException;
import com.library.model.Member;
import com.library.repository.MemberRepository;

import java.util.List;

public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Member registerMember(String memberId, String name, String email) {
        if (memberRepository.existsById(memberId)) {
            throw new DuplicateEntityException("A member with id " + memberId + " already exists.");
        }
        Member member = new Member(memberId, name, email);
        return memberRepository.save(member);
    }

    @Override
    public Member getMember(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    @Override
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }
}
