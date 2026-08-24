package com.example.hotelai.repository;

import com.example.hotelai.entity.StayRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StayRecordRepository extends JpaRepository<StayRecord, Long> {
    Optional<StayRecord> findByReservationId(Long reservationId);
}
