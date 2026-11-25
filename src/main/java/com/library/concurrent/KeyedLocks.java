package com.library.concurrent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * One lock per key (e.g. per book id), so unrelated books and members never block each other.
 * Locks are created lazily and kept for the lifetime of the registry (fine for an in-memory demo).
 */
public final class KeyedLocks {
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public <T> T withLock(String key, Supplier<T> action) {
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }
}
