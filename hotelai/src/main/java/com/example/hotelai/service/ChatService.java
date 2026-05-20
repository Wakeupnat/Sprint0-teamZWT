package com.example.hotelai.service;

import com.example.hotelai.dto.ChatResponse;

public interface ChatService {
    ChatResponse chat(String userId, String userMessage);   // MODIFIED: 返回 ChatResponse
}
