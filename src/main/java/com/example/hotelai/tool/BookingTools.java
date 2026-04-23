package com.example.hotelai.tool;

import com.example.hotelai.service.BookingService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class BookingTools {

    private final BookingService bookingService;

    public BookingTools(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // 用于在对话期间临时存储用户ID
    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();

    public static void setCurrentUserId(String userId) {
        currentUserId.set(userId);
    }

    public static void clear() {
        currentUserId.remove();
    }

    @Tool("预订酒店房间。需要提供房型名称（如大床房、双床房、套房）、入住日期（格式 yyyy-MM-dd）、离店日期（格式 yyyy-MM-dd）、房间数量（整数）")
    public String bookRoom(String roomTypeName, String checkInDateStr, String checkOutDateStr, int roomCount) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate checkIn = LocalDate.parse(checkInDateStr, formatter);
            LocalDate checkOut = LocalDate.parse(checkOutDateStr, formatter);

            // 修改点：从 ThreadLocal 中获取 userId，并作为 sessionId 传给 Service
            String sessionId = currentUserId.get();

            return bookingService.bookRoom(sessionId, sessionId, roomTypeName, checkIn, checkOut, roomCount);
        } catch (DateTimeParseException e) {
            return "日期格式错误，请提供 yyyy-MM-dd 格式的日期，例如 2025-05-01。";
        }
    }

    @Tool("查询当前用户的预订信息")
    public String queryReservation() {
        return bookingService.queryReservation(currentUserId.get());
    }
}