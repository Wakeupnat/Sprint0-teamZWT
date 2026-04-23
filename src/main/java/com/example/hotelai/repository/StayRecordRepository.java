package com.example.hotelai.repository;

import com.example.hotelai.entity.StayRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StayRecordRepository extends JpaRepository<StayRecord, Long> {
}
