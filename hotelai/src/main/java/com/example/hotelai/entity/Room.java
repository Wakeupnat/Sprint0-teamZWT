package com.example.hotelai.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ROOMS")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ROOM_NUMBER")
    private String roomNumber;   // 房号，如 "301"

    @Column(name = "FLOOR")
    private Integer floor;        // 楼层 1-5

    @Column(name = "ROOM_TYPE_ID")
    private Long roomTypeId;      // 关联房型

    @Column(name = "ROOM_TYPE_NAME")
    private String roomTypeName; // 房型名称

    private String status;        // AVAILABLE / OCCUPIED / CLEANING / MAINTENANCE
}
