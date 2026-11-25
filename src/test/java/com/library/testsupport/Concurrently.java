package com.library.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.IntConsumer;

/** Runs a task on N threads released at the same instant; fails the test if anything hangs (e.g. a deadlock). */
public final class Concurrently {
    private Concurrently() {}

    /** Returns the exception thrown by each task (null entry = task succeeded), indexed by task number. */
    public static List<Throwable> run(int threads, IntConsumer task) {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Throwable>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            final int index = i;
            futures.add(pool.submit(() -> {
                ready.countDown();
                go.await();
                try {
                    task.accept(index);
                    return null;
                } catch (Throwable t) {
                    return t;
                }
            }));
        }
        List<Throwable> results = new ArrayList<>();
        try {
            ready.await();
            go.countDown();
            for (Future<Throwable> f : futures) results.add(f.get(30, TimeUnit.SECONDS));
        } catch (TimeoutException e) {
            throw new AssertionError("tasks did not finish within 30s (deadlock or livelock?)", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new AssertionError(e);
        } finally {
            pool.shutdownNow();
        }
        return results;
    }

    public static long successes(List<Throwable> results) {
        return results.stream().filter(t -> t == null).count();
    }
}
