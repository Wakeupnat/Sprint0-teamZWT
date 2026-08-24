package com.example.hotelai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResult {
    private String message;       // 提示信息
    private Long reservationId;   // 生成的订单号（成功时）
    private String roomIds;       // 被占用房间的 ID（逗号分隔）
    private boolean success;      // 是否成功
}