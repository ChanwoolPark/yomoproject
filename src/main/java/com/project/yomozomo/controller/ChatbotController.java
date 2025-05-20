package com.project.yomozomo.controller;

import com.project.yomozomo.entity.ChatbotResponse;
import com.project.yomozomo.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/chat", produces = "application/json")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Autowired
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> getReply(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        Optional<ChatbotResponse> responseOptional = chatbotService.getChatbotResponse(userMessage);

        Map<String, String> responseBody = new HashMap<>();
        if (responseOptional.isPresent()) {
            responseBody.put("reply", responseOptional.get().getResponse());
        } else {
            responseBody.put("reply", "해당하는 답변을 찾을 수 없습니다.");
        }
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/all")
    public ResponseEntity<Iterable<ChatbotResponse>> getAllResponses() {
        return ResponseEntity.ok(chatbotService.getAllChatbotResponses());
    }

    @PostMapping("/add")
    public ResponseEntity<String> addResponse(@RequestParam String keyword, @RequestParam String response) {
        chatbotService.addChatbotResponse(keyword, response);
        return ResponseEntity.ok("자동 응답 메시지가 추가되었습니다.");
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