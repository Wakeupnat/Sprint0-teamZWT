package com.example.hotelai.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "ROOM_TYPES")
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;      // 大床房、双床房、套房
    private BigDecimal price; // 每晚价格
    private Integer totalRooms; // 总房间数
    private Integer availableRooms; // 当前可用房间数
}