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

    // ★★★ 新增字段：映射数据库的 SESSION_ID 列 ★★★
    @Column(name = "SESSION_ID", nullable = false)
    private String sessionId;
}