package com.library.util;

/** Small argument-validation helpers shared by the domain model. */
public final class Validation {
    private Validation() {}

    public static String requireNonBlank(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be null or blank");
        }
        return value.trim();
    }
}
