package com.example.hotelai.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "RESERVATIONS")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;          // 用户ID

    private String sessionId;       // 会话ID，用于关联匿名用户的预订

    @Column(name = "ROOM_TYPE_ID")
    private Long roomTypeId;

    @Column(name = "CHECK_IN_DATE")
    private LocalDate checkInDate;

    @Column(name = "CHECK_OUT_DATE")
    private LocalDate checkOutDate;

    @Column(name = "ROOM_COUNT")
    private Integer roomCount;

    private String status;          // BOOKED, CHECKED_IN, CANCELLED, COMPLETED

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    // ★★★ 新增字段：关联的房间ID列表（逗号分隔） ★★★
    @Column(name = "ROOM_IDS")
    private String roomIds;   // 如 "3,4,5"
}
