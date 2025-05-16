package com.project.yomozomo.controller;

import com.project.yomozomo.entity.ChatbotResponse;
import com.project.yomozomo.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Autowired
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addResponse(@RequestParam String keyword, @RequestParam String response) {
        chatbotService.addChatbotResponse(keyword, response);
        return ResponseEntity.ok("자동 응답 메시지가 추가되었습니다.");
    }

    @GetMapping("/response/{keyword}")
    public ResponseEntity<?> getResponse(@PathVariable String keyword) {
        Optional<ChatbotResponse> response = chatbotService.getChatbotResponse(keyword);
        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<Iterable<ChatbotResponse>> getAllResponses() {
        return ResponseEntity.ok(chatbotService.getAllChatbotResponses());
    }

    @PutMapping("/update/{keyword}")
    public ResponseEntity<String> updateResponse(@PathVariable String keyword, @RequestParam String response) {
        chatbotService.updateChatbotResponse(keyword, response);
        return ResponseEntity.ok("자동 응답 메시지가 수정되었습니다.");
    }

    @DeleteMapping("/delete/{keyword}")
    public ResponseEntity<String> deleteResponse(@PathVariable String keyword) {
        chatbotService.deleteChatbotResponse(keyword);
        return ResponseEntity.ok("자동 응답 메시지가 삭제되었습니다.");
    }
}