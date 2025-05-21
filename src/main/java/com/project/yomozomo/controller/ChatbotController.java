// src/main/java/com/project/yomozomo/controller/ChatbotController.java
package com.project.yomozomo.controller;

import com.project.yomozomo.entity.ChatbotQuestion;
import com.project.yomozomo.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping; // PostMapping 추가
import org.springframework.web.bind.annotation.ResponseBody; // @ResponseBody 추가 (JSON 응답용)

import java.util.Optional;

@Controller
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Autowired
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    // 첫 챗봇 화면을 로드하는 메서드
    @GetMapping("/chatbot")
    public String startChatbot(Model model) {
        Optional<ChatbotQuestion> initialQuestion = chatbotService.getInitialQuestion();
        if (initialQuestion.isPresent()) {
            // 이 초기 질문은 HTML에 고정된 초기 메시지 위에 표시될 수 있습니다.
            // 또는, 초기 질문은 백엔드에서 받아와 동적으로 첫 메시지로 표시하는 방식으로 변경할 수 있습니다.
            // 현재는 HTML에 고정 메시지가 있으므로, 이 부분은 필요에 따라 조정하세요.
            // model.addAttribute("question", initialQuestion.get());
        } else {
            model.addAttribute("errorMessage", "초기 챗봇 질문을 찾을 수 없습니다.");
        }
        return "chatbot"; // "chatbot.html" 템플릿을 렌더링
    }

    // (이전 옵션 선택 방식은 일단 주석 처리 또는 제거할 수 있습니다.
    //  새로운 텍스트 입력 방식이 주가 되므로)
    // @GetMapping("/chatbot/selectOption")
    // public String selectOption(@RequestParam Long optionId, Model model) {
    //     ChatbotService.ChatbotResponse response = chatbotService.processOptionSelection(optionId);
    //     // ... 응답 처리
    //     return "chatbot";
    // }

    // --- 새로운: 사용자 텍스트 입력을 처리하는 API 엔드포인트 ---
    @PostMapping("/chatbot/sendMessage") // POST 요청으로 사용자 메시지 받기
    @ResponseBody // JSON 형태로 응답 반환
    public ChatbotService.ChatbotResponse sendMessage(@RequestParam("message") String userMessage) {
        // 사용자 메시지를 서비스 계층으로 전달하여 답변을 찾습니다.
        return chatbotService.processUserText(userMessage);
    }
}