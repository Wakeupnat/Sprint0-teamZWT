package com.example.hotelai.repository;

import com.example.hotelai.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByFloor(Integer floor);

    List<Room> findByStatus(String status);

    List<Room> findByRoomTypeIdAndStatus(Long roomTypeId, String status);

    @Query("SELECT r.floor, r.status, COUNT(r) FROM Room r GROUP BY r.floor, r.status")
    List<Object[]> countByFloorAndStatus();

    @Query("SELECT DISTINCT r.floor FROM Room r ORDER BY r.floor")
    List<Integer> findAllFloors();

    List<Room> findByFloorOrderByRoomNumber(Integer floor);
}
