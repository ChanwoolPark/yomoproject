// src/main/java/com/project/yomozomo/controller/ChatbotController.java
package com.project.yomozomo.controller;

import com.project.yomozomo.entity.ChatbotQuestion;
import com.project.yomozomo.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Autowired
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    // This method handles the initial chatbot page
    // Renamed the mapping from "/chatbot" to "/chatbot-page" or similar
    @GetMapping("/chatbot-page") // <--- Changed mapping here
    public String startChatbot(Model model) {
        Optional<ChatbotQuestion> initialQuestion = chatbotService.getInitialQuestion();
        if (initialQuestion.isPresent()) {
            model.addAttribute("question", initialQuestion.get());
        } else {
            model.addAttribute("error", "초기 챗봇 질문을 찾을 수 없습니다.");
        }
        return "chatbot"; // Assuming "chatbot.html" is your Thymeleaf template
    }

    // This method handles subsequent option selections (API endpoint)
    @GetMapping("/api/chatbot/selectOption") // This path is likely for AJAX calls
    @ResponseBody
    public ChatbotService.ChatbotResponse selectOption(@RequestParam Long optionId) {
        return chatbotService.processOptionSelection(optionId);
    }
}