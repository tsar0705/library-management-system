package com.library;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MainSmokeTest {
    @Test @DisplayName("the console demo runs end to end and shows the business rules rejecting bad requests")
    void demoRuns() {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            Main.main(new String[0]);
        } finally {
            System.setOut(original);
        }
        String out = buffer.toString();
        assertTrue(out.contains("maximum of 2 active loans"));
        assertTrue(out.contains("reserved for another member"));
        assertTrue(out.contains("Outstanding fine on B2: 40.00"));
    }
}
