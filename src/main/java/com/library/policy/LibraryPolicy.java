package com.library.policy;

import java.math.BigDecimal;

/** Immutable, validated business rules. Amounts are currency-agnostic. */
public final class LibraryPolicy {
    public static final LibraryPolicy DEFAULT = new LibraryPolicy(14, 3, new BigDecimal("5.00"));

    private final int loanPeriodDays;
    private final int maxActiveLoansPerMember;
    private final BigDecimal finePerOverdueDay;

    public LibraryPolicy(int loanPeriodDays, int maxActiveLoansPerMember, BigDecimal finePerOverdueDay) {
        if (loanPeriodDays <= 0) throw new IllegalArgumentException("loanPeriodDays must be positive");
        if (maxActiveLoansPerMember <= 0) throw new IllegalArgumentException("maxActiveLoansPerMember must be positive");
        if (finePerOverdueDay == null || finePerOverdueDay.signum() < 0) {
            throw new IllegalArgumentException("finePerOverdueDay must be non-negative");
        }
        this.loanPeriodDays = loanPeriodDays;
        this.maxActiveLoansPerMember = maxActiveLoansPerMember;
        this.finePerOverdueDay = finePerOverdueDay;
    }

    public int getLoanPeriodDays() { return loanPeriodDays; }
    public int getMaxActiveLoansPerMember() { return maxActiveLoansPerMember; }
    public BigDecimal getFinePerOverdueDay() { return finePerOverdueDay; }
}
