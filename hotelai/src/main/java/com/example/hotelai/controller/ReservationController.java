package com.example.hotelai.controller;

import com.example.hotelai.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reservation")
@CrossOrigin(origins = "*")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    // 查询当前会话的所有预订（方便测试）
    @GetMapping("/my")
    public Map<String, Object> getMyReservations(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        if (sessionId == null) sessionId = "anonymous";
        return reservationService.getReservationsBySession(sessionId);
    }

    // 入住
    @PostMapping("/checkin")
    public Map<String, String> checkIn(@RequestBody Map<String, Long> request,
                                       @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        Long reservationId = request.get("reservationId");
        if (sessionId == null) sessionId = "anonymous";
        return reservationService.checkIn(reservationId, sessionId);
    }

    // 离店
    @PostMapping("/checkout")
    public Map<String, String> checkOut(@RequestBody Map<String, Long> request,
                                        @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        Long reservationId = request.get("reservationId");
        if (sessionId == null) sessionId = "anonymous";
        return reservationService.checkOut(reservationId, sessionId);
    }
}