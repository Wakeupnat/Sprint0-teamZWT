package com.example.hotelai.repository;

import com.example.hotelai.entity.HotelInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository<HotelInfo, Long> {
}
