package com.project.yomozomo.controller.chat;

import com.project.yomozomo.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // 이 줄로 변경합니다.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody; // API 메서드에 개별적으로 추가
import java.util.Map;

@Controller // @RestController 대신 @Controller를 사용합니다.
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Autowired
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    // 챗봇 HTML 페이지를 보여주는 메서드
    @GetMapping("/chatbot") // 이 매핑을 추가합니다.
    public String showChatbotPageAlternative() {
        return "chatbot"; // 동일하게 chatbot.html을 렌더링합니다.
    }

    // Map으로 반환 - API 엔드포인트
    @GetMapping("/chatbot/send")
    @ResponseBody // 이 메서드는 HTML이 아닌 데이터를 반환함을 명시합니다.
    public Map<String, Object> sendMessage(@RequestParam("message") String userMessage) {
        return chatbotService.processUserText(userMessage);
    }

    /*
    @PostMapping("/chatbot/send")
    @ResponseBody // 이 메서드는 HTML이 아닌 데이터를 반시합니다.
    public Map<String, Object> sendMessage(@RequestBody String userMessage) {
        return chatbotService.processUserText(userMessage);
    }
    */

    // Map으로 반환 - API 엔드포인트
    @GetMapping("/chatbot/init")
    @ResponseBody // 이 메서드는 HTML이 아닌 데이터를 반환함을 명시합니다.
    public Map<String, Object> getInitialMessage() {
        // ERROR: cannot find symbol method getInitialQuestion()
        // FIX: Change getInitialQuestion() to getInitialMessage()
        return chatbotService.getInitialMessage(); // 변경된 부분
    }

    // Map으로 반환 - API 엔드포인트
    @GetMapping("/chatbot/option")
    @ResponseBody // 이 메서드는 HTML이 아닌 데이터를 반환함을 명시합니다.
    public Map<String, Object> processOption(@RequestParam("optionId") Long optionId) {
        return chatbotService.processOptionSelection(optionId);
    }
}