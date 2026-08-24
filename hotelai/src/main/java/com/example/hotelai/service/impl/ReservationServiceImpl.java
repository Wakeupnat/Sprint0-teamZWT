package com.example.hotelai.service.impl;

import com.example.hotelai.entity.Reservation;
import com.example.hotelai.entity.Room;
import com.example.hotelai.entity.RoomType;
import com.example.hotelai.entity.StayRecord;
import com.example.hotelai.repository.ReservationRepository;
import com.example.hotelai.repository.RoomRepository;
import com.example.hotelai.repository.RoomTypeRepository;
import com.example.hotelai.repository.StayRecordRepository;
import com.example.hotelai.service.ReservationService;
import com.example.hotelai.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private StayRecordRepository stayRecordRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomService roomService;

    @Override
    public Map<String, Object> getReservationsBySession(String sessionId) {
        List<Reservation> reservations = reservationRepository.findByUserId(sessionId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Reservation r : reservations) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("roomTypeId", r.getRoomTypeId());
            Optional<RoomType> rt = roomTypeRepository.findById(r.getRoomTypeId());
            map.put("roomTypeName", rt.map(RoomType::getName).orElse("未知"));
            map.put("checkInDate", r.getCheckInDate());
            map.put("checkOutDate", r.getCheckOutDate());
            map.put("roomCount", r.getRoomCount());
            map.put("status", r.getStatus());
            map.put("createdAt", r.getCreatedAt());
            // ★ 查询具体房号
            if (r.getRoomIds() != null && !r.getRoomIds().isEmpty()) {
                List<Long> ids = Arrays.stream(r.getRoomIds().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .collect(Collectors.toList());
                List<Room> rooms = roomRepository.findAllById(ids);
                List<String> roomNumbers = rooms.stream()
                        .map(Room::getRoomNumber)
                        .sorted()
                        .collect(Collectors.toList());
                map.put("roomNumbers", roomNumbers);
            } else {
                map.put("roomNumbers", Collections.emptyList());
            }
            list.add(map);
        }
        return Map.of("code", "200", "data", list);
    }

    @Override
    @Transactional
    public Map<String, String> checkIn(Long reservationId, String sessionId) {
        Optional<Reservation> opt = reservationRepository.findById(reservationId);
        if (opt.isEmpty()) {
            return Map.of("code", "500", "message", "预订不存在");
        }
        Reservation reservation = opt.get();

        // 校验会话归属
        if (!reservation.getUserId().equals(sessionId)) {
            return Map.of("code", "500", "message", "无权操作此订单");
        }
        if (!"BOOKED".equals(reservation.getStatus())) {
            return Map.of("code", "500", "message", "当前订单状态不可办理入住");
        }
        if (reservation.getCheckInDate().isAfter(LocalDate.now())) {
            return Map.of("code", "500", "message", "未到入住日期，无法办理入住");
        }

        reservation.setStatus("CHECKED_IN");
        reservationRepository.save(reservation);

        StayRecord stayRecord = new StayRecord();
        stayRecord.setReservationId(reservationId);
        stayRecord.setActualCheckIn(LocalDateTime.now());
        stayRecordRepository.save(stayRecord);

        return Map.of("code", "200", "message", "入住办理成功");
    }

    @Override
    @Transactional
    public Map<String, String> checkOut(Long reservationId, String sessionId) {
        Optional<Reservation> opt = reservationRepository.findById(reservationId);
        if (opt.isEmpty()) {
            return Map.of("code", "500", "message", "预订不存在");
        }
        Reservation reservation = opt.get();

        if (!reservation.getUserId().equals(sessionId)) {
            return Map.of("code", "500", "message", "无权操作此订单");
        }
        if (!"CHECKED_IN".equals(reservation.getStatus())) {
            return Map.of("code", "500", "message", "当前订单未办理入住，无法离店");
        }

        // ★★★ 新增：释放房间，更新 ROOMS 表 ★★★
        String roomIds = reservation.getRoomIds();
        if (roomIds != null && !roomIds.isEmpty()) {
            roomService.releaseRooms(roomIds);
        }

        reservation.setStatus("COMPLETED");
        reservationRepository.save(reservation);

        Optional<StayRecord> stayRecordOpt = stayRecordRepository.findByReservationId(reservationId);
        stayRecordOpt.ifPresent(record -> {
            record.setActualCheckOut(LocalDateTime.now());
            stayRecordRepository.save(record);
        });

        return Map.of("code", "200", "message", "离店办理成功");
    }
}