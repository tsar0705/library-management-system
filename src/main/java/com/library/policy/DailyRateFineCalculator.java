package com.library.policy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/** Charges a flat rate for every started day after the due date. */
public final class DailyRateFineCalculator implements FineCalculator {
    private final BigDecimal ratePerDay;

    public DailyRateFineCalculator(BigDecimal ratePerDay) {
        if (ratePerDay == null || ratePerDay.signum() < 0) {
            throw new IllegalArgumentException("ratePerDay must be non-negative");
        }
        this.ratePerDay = ratePerDay;
    }

    @Override
    public BigDecimal calculateFine(LocalDateTime dueDate, LocalDateTime returnedAt) {
        if (!returnedAt.isAfter(dueDate)) {
            return BigDecimal.ZERO.setScale(2);
        }
        Duration late = Duration.between(dueDate, returnedAt);
        long wholeDays = late.toDays();
        long startedDays = late.minusDays(wholeDays).isZero() ? wholeDays : wholeDays + 1;
        return ratePerDay.multiply(BigDecimal.valueOf(startedDays)).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
