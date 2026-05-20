package com.example.hotelai.service;

import com.example.hotelai.entity.Room;
import com.example.hotelai.entity.RoomType;
import com.example.hotelai.repository.RoomRepository;
import com.example.hotelai.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;

    /**
     * 返回每层楼的房间状态分布
     * 结构: Map<floor, Map<status, count>>
     */
    public Map<String, Object> getFloorOverview() {
        Map<String, Object> result = new HashMap<>();
        List<Integer> floors = roomRepository.findAllFloors();

        for (Integer floor : floors) {
            List<Room> rooms = roomRepository.findByFloorOrderByRoomNumber(floor);
            Map<String, Object> floorData = new HashMap<>();
            floorData.put("rooms", rooms);
            floorData.put("total", rooms.size());
            floorData.put("available", rooms.stream().filter(r -> "AVAILABLE".equals(r.getStatus())).count());
            floorData.put("occupied", rooms.stream().filter(r -> "OCCUPIED".equals(r.getStatus())).count());
            floorData.put("cleaning", rooms.stream().filter(r -> "CLEANING".equals(r.getStatus())).count());
            floorData.put("maintenance", rooms.stream().filter(r -> "MAINTENANCE".equals(r.getStatus())).count());
            result.put("floor" + floor, floorData);
        }
        return result;
    }

    /**
     * 返回指定楼层的所有房间
     */
    public List<Room> getRoomsByFloor(Integer floor) {
        return roomRepository.findByFloorOrderByRoomNumber(floor);
    }

    /**
     * 返回所有房间，附带房型名称（供前端地图使用）
     */
    public List<Map<String, Object>> getAllRoomsWithTypeName() {
        List<Room> rooms = roomRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Room r : rooms) {
            Map<String, Object> map = new HashMap<>();
            map.put("roomNumber", r.getRoomNumber());
            map.put("floor", r.getFloor());
            map.put("status", r.getStatus());
            RoomType rt = roomTypeRepository.findById(r.getRoomTypeId()).orElse(null);
            map.put("roomTypeName", rt != null ? rt.getName() : "未知");
            result.add(map);
        }
        return result;
    }

    // ========== 新增：订单闭环方法 ==========

    /**
     * 占用指定房型的 N 间可用房间，状态改为 OCCUPIED
     * @return 被占用房间的 ID 列表（逗号分隔字符串）
     */
    @Transactional
    public String occupyRooms(Long roomTypeId, int count) {
        List<Room> available = roomRepository.findByRoomTypeIdAndStatus(roomTypeId, "AVAILABLE");
        if (available.size() < count) {
            throw new IllegalStateException("可用房间不足，需要" + count + "间，仅剩" + available.size() + "间");
        }
        StringBuilder ids = new StringBuilder();
        for (int i = 0; i < count; i++) {
            Room room = available.get(i);
            room.setStatus("OCCUPIED");
            roomRepository.save(room);
            if (ids.length() > 0) ids.append(",");
            ids.append(room.getId());
        }
        // 同步更新 RoomType.availableRooms
        refreshAvailableRooms(roomTypeId);
        return ids.toString();
    }

    /**
     * 释放指定房间（逗号分隔的 ID 字符串），状态改回 AVAILABLE
     */
    @Transactional
    public void releaseRooms(String roomIds) {
        if (roomIds == null || roomIds.isEmpty()) return;
        String[] idArray = roomIds.split(",");
        for (String idStr : idArray) {
            Long id = Long.valueOf(idStr.trim());
            roomRepository.findById(id).ifPresent(room -> {
                room.setStatus("AVAILABLE");
                roomRepository.save(room);
            });
        }
        // 同步更新对应房型的 availableRooms
        if (idArray.length > 0) {
            roomRepository.findById(Long.valueOf(idArray[0].trim())).ifPresent(room -> {
                refreshAvailableRooms(room.getRoomTypeId());
            });
        }
    }

    /**
     * 根据 ROOMS 表实时重新计算 RoomType.availableRooms
     */
    @Transactional
    public void refreshAvailableRooms(Long roomTypeId) {
        RoomType rt = roomTypeRepository.findById(roomTypeId).orElse(null);
        if (rt == null) return;
        long available = roomRepository.findByRoomTypeIdAndStatus(roomTypeId, "AVAILABLE").size();
        rt.setAvailableRooms((int) available);
        roomTypeRepository.save(rt);
    }
}
