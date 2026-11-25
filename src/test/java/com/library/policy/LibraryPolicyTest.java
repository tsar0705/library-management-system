package com.library.policy;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LibraryPolicyTest {
    @Test @DisplayName("default policy values")
    void defaults() {
        assertEquals(14, LibraryPolicy.DEFAULT.getLoanPeriodDays());
        assertEquals(3, LibraryPolicy.DEFAULT.getMaxActiveLoansPerMember());
        assertEquals(new BigDecimal("5.00"), LibraryPolicy.DEFAULT.getFinePerOverdueDay());
    }

    @Test @DisplayName("invalid policies are rejected")
    void invalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new LibraryPolicy(0, 3, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> new LibraryPolicy(14, 0, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> new LibraryPolicy(14, 3, new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> new LibraryPolicy(14, 3, null));
    }
}
