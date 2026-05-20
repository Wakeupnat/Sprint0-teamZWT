package com.example.hotelai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String answer;               // AI 自然语言回复
    private String action;               // BOOK_ROOM, QUERY_RESERVATION, CHECK_IN, CHECK_OUT, OTHER
    private Map<String, Object> data;    // 附带的结构化表单数据
}