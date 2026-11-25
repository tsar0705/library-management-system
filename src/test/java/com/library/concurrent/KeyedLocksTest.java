package com.library.concurrent;

import static org.junit.jupiter.api.Assertions.*;

import com.library.testsupport.Concurrently;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KeyedLocksTest {
    @Test @DisplayName("the same key is mutually exclusive: no lost updates")
    void sameKeyIsExclusive() {
        KeyedLocks locks = new KeyedLocks();
        int[] counter = {0};
        Concurrently.run(16, i -> {
            for (int n = 0; n < 500; n++) locks.withLock("k", () -> { counter[0]++; return null; });
        });
        assertEquals(16 * 500, counter[0]);
    }

    @Test @DisplayName("different keys do not block each other")
    void differentKeysIndependent() throws Exception {
        KeyedLocks locks = new KeyedLocks();
        CountDownLatch holding = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread holder = new Thread(() -> locks.withLock("A", () -> {
            holding.countDown();
            try { release.await(10, TimeUnit.SECONDS); } catch (InterruptedException ignored) { }
            return null;
        }));
        holder.start();
        assertTrue(holding.await(10, TimeUnit.SECONDS));
        String result = locks.withLock("B", () -> "acquired while A is held");   // would hang if keys shared a lock
        assertEquals("acquired while A is held", result);
        release.countDown();
        holder.join(10_000);
    }

    @Test @DisplayName("the lock is released when the action throws")
    void releasedOnException() {
        KeyedLocks locks = new KeyedLocks();
        assertThrows(IllegalStateException.class, () -> locks.withLock("k", () -> { throw new IllegalStateException("boom"); }));
        assertEquals("ok", locks.withLock("k", () -> "ok"));
    }
}
