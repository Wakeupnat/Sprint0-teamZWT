package com.example.hotelai.service.impl;

import com.example.hotelai.dto.ChatResponse;
import com.example.hotelai.service.Assistant;
import com.example.hotelai.service.ChatService;
import com.example.hotelai.tool.BookingTools;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

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

        // 为每个 session 分配独立的对话记忆（保留最近20条消息）
        ChatMemoryProvider memoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .build();

        // 构建 AI 服务时，传入已经注入好依赖的 bookingTools 实例
        // 注意：这里不再使用 new BookingTools()
        assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .tools(bookingTools)
                .chatMemoryProvider(memoryProvider)
                .build();
    }

    @Override
    public ChatResponse chat(String userId, String userMessage) {
        BookingTools.setCurrentUserId(userId);
        try {
            // ★ 注入当前真实日期，让 AI 正确解析"今天/前天/后天"
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String enrichedMessage = "【系统日期：" + today + "】" + userMessage;

            // 传入 userId 作为 memoryId，让 AI 记住这个 session 的上下文
            String answer = assistant.chat(userId, enrichedMessage);

            Map<String, Object> structured = BookingTools.getAndClearLastStructuredData();
            if (structured != null) {
                String action = (String) structured.get("action");
                Map<String, Object> data = (Map<String, Object>) structured.get("data");

                // ★ 兜底：当预订失败时，强制覆盖 AI 可能编造的成功回复
                if ("BOOK_ROOM_FAILED".equals(action) && data != null) {
                    String reason = (String) data.get("reason");
                    answer = "预订未成功：" + (reason != null ? reason : "未知错误，请重试");
                }
                return new ChatResponse(answer, action, data);
            } else {
                return new ChatResponse(answer, "OTHER", null);
            }
        } finally {
            BookingTools.clear();
        }
    }
}