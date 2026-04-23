package com.example.hotelai.controller;

import com.example.hotelai.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/sync")
    public Map<String, String> chat(@RequestBody Map<String, String> request,
                                    @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        String question = request.get("question");
        if (sessionId == null) sessionId = "anonymous";
        String answer = chatService.chat(sessionId, question);
        return Map.of("answer", answer);
    }
}