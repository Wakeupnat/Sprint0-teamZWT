package com.example.hotelai.service;

import com.example.hotelai.entity.RoomType;
import com.example.hotelai.repository.ReservationRepository;
import com.example.hotelai.repository.RoomRepository;
import com.example.hotelai.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoomTypeService {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomRepository roomRepository;

    public List<Map<String, Object>> getAllWithAvailability() {
        List<RoomType> types = roomTypeRepository.findAll();

        return types.stream().map(type -> {
            // 直接从 ROOMS 表统计当前 AVAILABLE 状态的房间数，与地图显示保持一致
            long availableCount = roomRepository.findByRoomTypeIdAndStatus(type.getId(), "AVAILABLE").size();

            Map<String, Object> map = new HashMap<>();
            map.put("id", type.getId());
            map.put("name", type.getName());
            map.put("price", type.getPrice());
            map.put("totalRooms", type.getTotalRooms());
            map.put("availableRooms", (int) availableCount);
            return map;
        }).collect(Collectors.toList());
    }

    public Map<String, Integer> getAvailabilityMap() {
        List<RoomType> types = roomTypeRepository.findAll();

        Map<String, Integer> result = new HashMap<>();
        for (RoomType type : types) {
            long availableCount = roomRepository.findByRoomTypeIdAndStatus(type.getId(), "AVAILABLE").size();
            result.put(type.getName(), (int) availableCount);
        }
        return result;
    }
}
