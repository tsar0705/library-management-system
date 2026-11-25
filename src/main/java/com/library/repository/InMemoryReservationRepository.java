package com.library.repository;

import com.library.model.Reservation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryReservationRepository implements ReservationRepository {
    private final Map<String, Deque<Reservation>> queues = new ConcurrentHashMap<>();

    @Override
    public boolean enqueueIfAbsent(Reservation reservation) {
        Deque<Reservation> queue = queues.computeIfAbsent(reservation.getBookId(), k -> new ArrayDeque<>());
        synchronized (queue) {
            if (queue.stream().anyMatch(r -> r.getMemberId().equals(reservation.getMemberId()))) {
                return false;
            }
            queue.addLast(reservation);
            return true;
        }
    }

    @Override
    public Optional<Reservation> peek(String bookId) {
        Deque<Reservation> queue = queues.get(bookId);
        if (queue == null) return Optional.empty();
        synchronized (queue) {
            return Optional.ofNullable(queue.peekFirst());
        }
    }

    @Override
    public Optional<Reservation> poll(String bookId) {
        Deque<Reservation> queue = queues.get(bookId);
        if (queue == null) return Optional.empty();
        synchronized (queue) {
            return Optional.ofNullable(queue.pollFirst());
        }
    }

    @Override
    public boolean remove(String bookId, String memberId) {
        Deque<Reservation> queue = queues.get(bookId);
        if (queue == null) return false;
        synchronized (queue) {
            return queue.removeIf(r -> r.getMemberId().equals(memberId));
        }
    }

    @Override
    public List<Reservation> findByBookId(String bookId) {
        Deque<Reservation> queue = queues.get(bookId);
        if (queue == null) return new ArrayList<>();
        synchronized (queue) {
            return new ArrayList<>(queue);
        }
    }
}
