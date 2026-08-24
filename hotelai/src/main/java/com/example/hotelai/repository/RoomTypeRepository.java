package com.example.hotelai.repository;

import com.example.hotelai.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    Optional<RoomType> findByNameContainingIgnoreCase(String name);
}