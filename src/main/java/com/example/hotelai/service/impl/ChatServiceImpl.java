package com.example.hotelai.service.impl;

import com.example.hotelai.service.Assistant;
import com.example.hotelai.service.ChatService;
import com.example.hotelai.tool.BookingTools;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {

    @Value("${langchain4j.community.dashscope.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.community.dashscope.chat-model.model-name}")
    private String modelName;

    private Assistant assistant;

    private final BookingTools bookingTools;

    // 构造器注入 BookingTools
    public ChatServiceImpl(BookingTools bookingTools) {
        this.bookingTools = bookingTools;
    }

    @PostConstruct
    public void init() {
        // 构建模型
        ChatLanguageModel model = QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();

        // 构建 AI 服务时，传入已经注入好依赖的 bookingTools 实例
        // 注意：这里不再使用 new BookingTools()
        assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .tools(bookingTools)
                .build();
    }

    @Override
    public String chat(String userId, String userMessage) {
        BookingTools.setCurrentUserId(userId);
        try {
            return assistant.chat(userMessage);
        } finally {
            BookingTools.clear();
        }
    }
}