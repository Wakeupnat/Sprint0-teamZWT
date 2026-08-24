package com.example.hotelai.controller;

import com.example.hotelai.entity.Room;
import com.example.hotelai.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/floors")
    public Map<String, Object> getFloorOverview() {
        return roomService.getFloorOverview();
    }

    @GetMapping("/floor/{floor}")
    public List<Room> getRoomsByFloor(@PathVariable Integer floor) {
        return roomService.getRoomsByFloor(floor);
    }

    @GetMapping("/all")
    public List<Map<String, Object>> getAllRooms() {
        return roomService.getAllRoomsWithTypeName();
    }
}
