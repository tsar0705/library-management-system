package com.library.policy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Strategy for pricing late returns; swap the implementation without touching the services. */
public interface FineCalculator {
    BigDecimal calculateFine(LocalDateTime dueDate, LocalDateTime returnedAt);
}
