package com.example.hotelai.service;

import com.example.hotelai.dto.BookingResult;
import com.example.hotelai.entity.Reservation;
import com.example.hotelai.entity.RoomType;
import com.example.hotelai.repository.ReservationRepository;
import com.example.hotelai.repository.RoomTypeRepository;
import com.example.hotelai.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomService roomService;



    /**
     * 预订房间
     * @param sessionId 会话ID，用于关联预订记录
     */
    // 修改点：增加了 sessionId 参数
    public String bookRoom(String userId, String sessionId, String roomTypeName, LocalDate checkIn, LocalDate checkOut, int roomCount) {
        // 1. 查找房型
        Optional<RoomType> roomTypeOpt = roomTypeRepository.findByNameContainingIgnoreCase(roomTypeName);
        if (roomTypeOpt.isEmpty()) {
            return "抱歉，我们没有“" + roomTypeName + "”这种房型，请选择大床房、双床房或套房。";
        }
        RoomType roomType = roomTypeOpt.get();

        // 2. 日期校验
        if (checkIn.isBefore(LocalDate.now())) {
            return "入住日期不能早于今天。";
        }
        if (checkOut.isBefore(checkIn.plusDays(1))) {
            return "离店日期必须至少比入住日期晚一天。";
        }

        // 3. 检查空房
        int conflicted = reservationRepository.countConflictingReservations(roomType.getId(), checkIn, checkOut);
        int available = roomType.getTotalRooms() - conflicted;
        if (available < roomCount) {
            return "抱歉，所选时间段内“" + roomTypeName + "”只剩 " + available + " 间，无法预订 " + roomCount + " 间。";
        }

        // 4. 创建预订记录
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setSessionId(sessionId); // 修改点：设置 SessionId，这是修复报错的关键
        reservation.setRoomTypeId(roomType.getId());
        reservation.setCheckInDate(checkIn);
        reservation.setCheckOutDate(checkOut);
        reservation.setRoomCount(roomCount);
        reservation.setStatus("BOOKED");
        reservation.setCreatedAt(LocalDateTime.now());

        reservationRepository.save(reservation);

        return bookRoomWithResult(userId, sessionId, roomTypeName, checkIn, checkOut, roomCount).getMessage();
    }

    // ========== 新增方法：返回结构化的预订结果 ==========
    // NEW
    public BookingResult bookRoomWithResult(String userId, String sessionId, String roomTypeName,
                                            LocalDate checkIn, LocalDate checkOut, int roomCount) {
        // 1. 查找房型
        Optional<RoomType> roomTypeOpt = roomTypeRepository.findByNameContainingIgnoreCase(roomTypeName);
        if (roomTypeOpt.isEmpty()) {
            return new BookingResult("抱歉，我们没有“" + roomTypeName + "”这种房型，请选择大床房、双床房或套房。", null, null, false);
        }
        RoomType roomType = roomTypeOpt.get();

        // 2. 日期校验
        LocalDate today = LocalDate.now();
        if (checkIn.isBefore(today)) {
            return new BookingResult("入住日期不能早于今天（" + today + "），请提供 " + today + " 或之后的日期。", null, null, false);
        }
        if (checkOut.isBefore(checkIn.plusDays(1))) {
            return new BookingResult("离店日期必须至少比入住日期晚一天。", null, null, false);
        }

        // 3. 空房检查
        int conflicted = reservationRepository.countConflictingReservations(roomType.getId(), checkIn, checkOut);
        int available = roomType.getTotalRooms() - conflicted;
        if (available < roomCount) {
            return new BookingResult("抱歉，所选时间段内“" + roomTypeName + "”只剩 " + available + " 间，无法预订 " + roomCount + " 间。", null, null, false);
        }

        // 4. 保存预订
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setSessionId(sessionId);
        reservation.setRoomTypeId(roomType.getId());
        reservation.setCheckInDate(checkIn);
        reservation.setCheckOutDate(checkOut);
        reservation.setRoomCount(roomCount);
        reservation.setStatus("BOOKED");
        reservation.setCreatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        // ★★★ 新增：占用具体房间，更新 ROOMS 表 ★★★
        String roomIds = null;
        try {
            roomIds = roomService.occupyRooms(roomType.getId(), roomCount);
            reservation.setRoomIds(roomIds);
            reservationRepository.save(reservation);
        } catch (IllegalStateException e) {
            return new BookingResult(e.getMessage(), reservation.getId(), null, false);
        }

        String message = "预订成功！订单号：" + reservation.getId() + "，房型：" + roomType.getName() +
                "，入住：" + checkIn + "，离店：" + checkOut + "，共" + roomCount + "间。";
        if (roomIds != null && !roomIds.isEmpty()) {
            message += " 房间号：" + roomIds.replace(",", "、");
        }
        return new BookingResult(message, reservation.getId(), roomIds, true);
    }



    /**
     * 查询用户的预订
     */
    public String queryReservation(String userId) {
        var reservations = reservationRepository.findAll().stream()
                .filter(r -> r.getUserId().equals(userId))
                .toList();
        if (reservations.isEmpty()) {
            return "您当前没有有效预订。";
        }
        Reservation r = reservations.get(0);
        Optional<RoomType> rt = roomTypeRepository.findById(r.getRoomTypeId());
        String roomName = rt.map(RoomType::getName).orElse("未知房型");
        return "您的订单号：" + r.getId() + "，房型：" + roomName +
                "，入住：" + r.getCheckInDate() + "，离店：" + r.getCheckOutDate() +
                "，状态：" + r.getStatus();
    }
}