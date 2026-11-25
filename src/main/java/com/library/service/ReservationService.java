package com.library.service;

import com.library.model.Reservation;
import java.util.List;

public interface ReservationService {
    /** Joins the FIFO queue for a book that is currently out (or already has a queue). */
    Reservation reserveBook(String bookId, String memberId);
    void cancelReservation(String bookId, String memberId);
    List<Reservation> getQueueForBook(String bookId);
}
