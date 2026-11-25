package com.library.repository;

import com.library.model.Reservation;
import java.util.List;
import java.util.Optional;

/** FIFO reservation queues, one per book. */
public interface ReservationRepository {
    /** Adds to the back of the queue; returns false (and changes nothing) if the member is already queued for the book. */
    boolean enqueueIfAbsent(Reservation reservation);
    Optional<Reservation> peek(String bookId);
    Optional<Reservation> poll(String bookId);
    boolean remove(String bookId, String memberId);
    List<Reservation> findByBookId(String bookId);
}
