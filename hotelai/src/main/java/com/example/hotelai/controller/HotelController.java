package com.example.hotelai.controller;

import com.example.hotelai.entity.HotelInfo;
import com.example.hotelai.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotel")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HotelController {

    private final HotelService hotelService;

    @GetMapping("/info")
    public HotelInfo getHotelInfo() {
        return hotelService.getHotelInfo();
    }
}
