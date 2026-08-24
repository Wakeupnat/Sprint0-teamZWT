package com.example.hotelai.service;

import com.example.hotelai.entity.HotelInfo;
import com.example.hotelai.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelInfo getHotelInfo() {
        return hotelRepository.findById(1L).orElse(null);
    }
}
