package com.library.service;

import com.library.model.Member;

import java.util.List;

/**
 * Responsible only for member registration/lookup.
 */
public interface MemberService {

    Member registerMember(String memberId, String name, String email);

    Member getMember(String memberId);

    List<Member> getAllMembers();
}
