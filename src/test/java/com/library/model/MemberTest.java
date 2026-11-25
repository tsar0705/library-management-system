package com.library.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {
    @Test @DisplayName("a valid member is created and trimmed")
    void validMember() {
        Member m = new Member(" M1 ", " Asha ", "asha@example.com");
        assertEquals("M1", m.getMemberId());
        assertEquals("Asha", m.getName());
    }

    @Test @DisplayName("malformed e-mail addresses are rejected")
    void rejectsBadEmail() {
        for (String bad : new String[]{"plain", "a@b", "@example.com", "a b@example.com", "a@@example.com"}) {
            assertThrows(IllegalArgumentException.class, () -> new Member("M1", "Asha", bad), bad);
        }
    }

    @Test @DisplayName("blank id or name is rejected")
    void rejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Member("", "Asha", "a@example.com"));
        assertThrows(IllegalArgumentException.class, () -> new Member("M1", "  ", "a@example.com"));
    }

    @Test @DisplayName("members are equal when their ids are equal")
    void equalityIsById() {
        assertEquals(new Member("M1", "A", "a@example.com"), new Member("M1", "B", "b@example.com"));
    }
}
