package com.library.model;

import com.library.util.Validation;
import java.util.regex.Pattern;

public class Member {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final String memberId;
    private final String name;
    private final String email;

    public Member(String memberId, String name, String email) {
        this.memberId = Validation.requireNonBlank(memberId, "memberId");
        this.name = Validation.requireNonBlank(name, "name");
        this.email = Validation.requireNonBlank(email, "email");
        if (!EMAIL.matcher(this.email).matches()) {
            throw new IllegalArgumentException("email is not a valid address: " + email);
        }
    }

    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Member && memberId.equals(((Member) o).memberId));
    }

    @Override
    public int hashCode() { return memberId.hashCode(); }

    @Override
    public String toString() {
        return String.format("Member{id='%s', name='%s', email='%s'}", memberId, name, email);
    }
}
