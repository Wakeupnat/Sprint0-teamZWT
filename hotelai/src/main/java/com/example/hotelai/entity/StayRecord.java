package com.example.hotelai.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "STAY_RECORDS")
public class StayRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long reservationId;
    private LocalDateTime actualCheckIn;
    private LocalDateTime actualCheckOut;
}