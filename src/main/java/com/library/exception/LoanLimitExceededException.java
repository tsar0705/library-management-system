package com.library.exception;

public class LoanLimitExceededException extends LibraryException {
    public LoanLimitExceededException(String memberId, int limit) {
        super("Member " + memberId + " already has the maximum of " + limit + " active loans.");
    }
}
