package com.library.service;

import static org.junit.jupiter.api.Assertions.*;

import com.library.exception.DuplicateEntityException;
import com.library.exception.MemberNotFoundException;
import com.library.testsupport.TestLibrary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberServiceTest {
    private final TestLibrary lib = new TestLibrary().withMembers(2);

    @Test @DisplayName("members can be registered and read back")
    void register() {
        assertEquals("Member 1", lib.memberService.getMember("M1").getName());
        assertEquals(2, lib.memberService.getAllMembers().size());
    }

    @Test @DisplayName("duplicate member ids are rejected")
    void duplicate() {
        assertThrows(DuplicateEntityException.class, () -> lib.memberService.registerMember("M1", "X", "x@example.com"));
    }

    @Test @DisplayName("an unknown member raises MemberNotFoundException")
    void unknown() {
        assertThrows(MemberNotFoundException.class, () -> lib.memberService.getMember("M99"));
    }
}
