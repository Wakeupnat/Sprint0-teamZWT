package com.example.hotelai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "HOTEL_INFO")
public class HotelInfo {
    @Id
    private Long id;
    private String name;
    private String address;
    private String phone;
    @Column(length = 1000)
    private String description;
    private BigDecimal rating;
}
