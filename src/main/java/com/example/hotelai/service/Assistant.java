package com.example.hotelai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface Assistant {
    @SystemMessage("你是一个酒店AI客服助手，你可以帮助用户预订酒店、查询预订信息。当用户表达预订意愿时，调用 bookRoom 工具；当用户询问预订状态时，调用 queryReservation 工具。请用友好、专业的语气回复。")
    String chat(@UserMessage String userMessage);
}