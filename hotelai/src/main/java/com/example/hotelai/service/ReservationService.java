package com.example.hotelai.service;

import java.util.Map;

public interface ReservationService {
    Map<String, Object> getReservationsBySession(String sessionId);
    Map<String, String> checkIn(Long reservationId, String sessionId);
    Map<String, String> checkOut(Long reservationId, String sessionId);
}