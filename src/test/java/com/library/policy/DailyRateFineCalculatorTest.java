package com.library.policy;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DailyRateFineCalculatorTest {
    private static final LocalDateTime DUE = LocalDateTime.of(2026, 1, 15, 10, 0);
    private final FineCalculator calc = new DailyRateFineCalculator(new BigDecimal("5.00"));

    private void assertFine(String expected, LocalDateTime returnedAt) {
        assertEquals(new BigDecimal(expected), calc.calculateFine(DUE, returnedAt));
    }

    @Test @DisplayName("no fine when returned early or exactly on the due date")
    void noFineOnTime() {
        assertFine("0.00", DUE.minusDays(2));
        assertFine("0.00", DUE);
    }

    @Test @DisplayName("every started day late costs one daily rate")
    void startedDaysCount() {
        assertFine("5.00", DUE.plusSeconds(1));
        assertFine("5.00", DUE.plusDays(1));
        assertFine("10.00", DUE.plusDays(1).plusSeconds(1));
        assertFine("15.00", DUE.plusDays(2).plusHours(3));
    }

    @Test @DisplayName("a zero rate never charges")
    void zeroRate() {
        assertEquals(new BigDecimal("0.00"), new DailyRateFineCalculator(BigDecimal.ZERO).calculateFine(DUE, DUE.plusDays(9)));
    }

    @Test @DisplayName("negative rate is rejected")
    void negativeRate() {
        assertThrows(IllegalArgumentException.class, () -> new DailyRateFineCalculator(new BigDecimal("-0.01")));
    }
}
