package com.example.hotelai.repository;
import com.example.hotelai.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 查询某个时间段内已预订的房间数量（简化：只查状态为BOOKED或CHECKED_IN的）
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.roomTypeId = :roomTypeId AND r.status IN ('BOOKED','CHECKED_IN') " +
            "AND r.checkInDate < :checkOutDate AND r.checkOutDate > :checkInDate")
    int countConflictingReservations(@Param("roomTypeId") Long roomTypeId,
                                     @Param("checkInDate") LocalDate checkInDate,
                                     @Param("checkOutDate") LocalDate checkOutDate);
    List<Reservation> findByUserId(String userId);
}