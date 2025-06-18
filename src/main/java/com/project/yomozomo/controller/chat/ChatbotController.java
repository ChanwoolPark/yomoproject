package com.project.yomozomo.controller.chat;

import com.project.yomozomo.dto.ChatbotResponse; // ChatbotResponse DTO 경로 확인
import com.project.yomozomo.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @GetMapping("/chatbot")
    public String chatbotPage(Model model) {
        // Thymeleaf 템플릿 반환
        return "chatbot"; // src/main/resources/templates/chatbot.html
    }

    @GetMapping("/chatbot/init")
    @ResponseBody // JSON 응답을 위한 어노테이션
    public ResponseEntity<ChatbotResponse> initChatbot() {
        ChatbotResponse response = chatbotService.getInitialMessage();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chatbot/send")
    @ResponseBody // JSON 응답을 위한 어노테이션
    public ResponseEntity<ChatbotResponse> sendMessage(@RequestParam("message") String userMessage) {
        // userMessage를 ChatbotService의 processMessage 메서드로 전달
        // 오류 발생 부분: processUserText -> processMessage
        ChatbotResponse response = chatbotService.processMessage(userMessage); // <-- 이 부분을 수정했습니다.
        return ResponseEntity.ok(response);
    }
}