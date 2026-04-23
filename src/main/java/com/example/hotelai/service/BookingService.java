package com.example.hotelai.service;

import com.example.hotelai.entity.Reservation;
import com.example.hotelai.entity.RoomType;
import com.example.hotelai.repository.ReservationRepository;
import com.example.hotelai.repository.RoomTypeRepository;
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

        return "预订成功！订单号：" + reservation.getId() + "，房型：" + roomType.getName() +
                "，入住：" + checkIn + "，离店：" + checkOut + "，共" + roomCount + "间。";
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