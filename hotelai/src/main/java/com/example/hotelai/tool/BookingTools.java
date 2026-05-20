package com.example.hotelai.tool;

import com.example.hotelai.dto.BookingResult;
import com.example.hotelai.service.BookingService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.hotelai.service.ReservationService;

@Component
public class BookingTools {

    private final BookingService bookingService;
    private final ReservationService reservationService;

    // NEW: 用于暂存当前会话的结构化数据
    private static final ThreadLocal<Map<String, Object>> lastStructuredData = new ThreadLocal<>();
    // 用于在对话期间临时存储用户ID
    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();

    public BookingTools(BookingService bookingService, ReservationService reservationService) {
        this.bookingService = bookingService;
        this.reservationService = reservationService;
    }

    public static void setCurrentUserId(String userId) {
        currentUserId.set(userId);
    }

    public static void clear() {
        currentUserId.remove();
    }

    // NEW: 获取并清除最后一次的结构化数据
    public static Map<String, Object> getAndClearLastStructuredData() {
        Map<String, Object> data = lastStructuredData.get();
        lastStructuredData.remove();
        return data;
    }

    // NEW: 内部方法，存入结构化数据
    private void setStructuredData(String action, Map<String, Object> data) {
        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("action", action);
        wrapper.put("data", data);
        lastStructuredData.set(wrapper);
    }


    @Tool("预订酒店房间。需要提供房型名称（如大床房、双床房、套房）、入住日期（格式 yyyy-MM-dd）、离店日期（格式 yyyy-MM-dd）、房间数量（整数）")
    public String bookRoom(String roomTypeName, String checkInDateStr, String checkOutDateStr, int roomCount) {
        String sessionId = currentUserId.get();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate checkIn = LocalDate.parse(checkInDateStr, formatter);
            LocalDate checkOut = LocalDate.parse(checkOutDateStr, formatter);

            // MODIFIED: 调用带结果的方法
            BookingResult result = bookingService.bookRoomWithResult(sessionId, sessionId, roomTypeName, checkIn, checkOut, roomCount);
            if (result.isSuccess()) {
                Map<String, Object> data = new HashMap<>();
                data.put("reservationId", result.getReservationId());
                data.put("roomType", roomTypeName);
                data.put("checkInDate", checkInDateStr);
                data.put("checkOutDate", checkOutDateStr);
                data.put("roomCount", roomCount);
                data.put("roomIds", result.getRoomIds());   // ★ 供前端显示具体房号
                setStructuredData("BOOK_ROOM", data);
            } else {
                setStructuredData("BOOK_ROOM_FAILED", Map.of("reason", result.getMessage()));
            }
            return result.getMessage();
        } catch (DateTimeParseException e) {
            return "日期格式错误，请提供 yyyy-MM-dd 格式的日期，例如 2025-05-01。";
        }
    }

    @Tool("查询当前用户的预订信息")
    public String queryReservation() {
        String userId = currentUserId.get();
        // MODIFIED: 通过 ReservationService 获取结构化列表
        Map<String, Object> reservationsResult = reservationService.getReservationsBySession(userId);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) reservationsResult.get("data");
        setStructuredData("QUERY_RESERVATION", Map.of("reservations", list));
        return bookingService.queryReservation(userId);
    }

    @Tool("办理入住。需要提供预订ID（整数），只有状态为'BOOKED'且入住日期不晚于今天的订单才能入住。")
    public String checkIn(Long reservationId) {
        String sessionId = currentUserId.get();
        Map<String, String> result = reservationService.checkIn(reservationId, sessionId);
        if ("200".equals(result.get("code"))) {
            setStructuredData("CHECK_IN", Map.of("reservationId", reservationId, "status", "SUCCESS"));
            return "入住成功！欢迎入住，祝您愉快。";
        } else {
            setStructuredData("CHECK_IN", Map.of("reservationId", reservationId, "status", "FAILED", "reason", result.get("message")));
            return "入住失败：" + result.get("message");
        }
    }

    @Tool("办理离店。需要提供预订ID（整数）。只有状态为'CHECKED_IN'的订单才能离店。")
    public String checkOut(Long reservationId) {
        String sessionId = currentUserId.get();
        Map<String, String> result = reservationService.checkOut(reservationId, sessionId);
        if ("200".equals(result.get("code"))) {
            setStructuredData("CHECK_OUT", Map.of("reservationId", reservationId, "status", "SUCCESS"));
            return "离店成功，感谢您的入住，期待下次光临。";
        } else {
            setStructuredData("CHECK_OUT", Map.of("reservationId", reservationId, "status", "FAILED", "reason", result.get("message")));
            return "离店失败：" + result.get("message");
        }
    }
}