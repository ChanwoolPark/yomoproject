package com.project.yomozomo.service;

import com.project.yomozomo.entity.ChatbotResponse;
import com.project.yomozomo.repository.ChatbotResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChatbotService {

    private final ChatbotResponseRepository chatbotResponseRepository;

    @Autowired
    public ChatbotService(ChatbotResponseRepository chatbotResponseRepository) {
        this.chatbotResponseRepository = chatbotResponseRepository;
    }

    public Optional<ChatbotResponse> getChatbotResponse(String keyword) {
        return chatbotResponseRepository.findById(keyword); // Primary Key로 데이터 조회
    }

    public Iterable<ChatbotResponse> getAllChatbotResponses() {
        return chatbotResponseRepository.findAll(); // 모든 데이터 조회
    }

    public void addChatbotResponse(String keyword, String response) {
        ChatbotResponse newResponse = new ChatbotResponse();
        newResponse.setKeyword(keyword);
        newResponse.setResponse(response);
        chatbotResponseRepository.save(newResponse); // 데이터 저장 (Create & Update)
    }

    public void updateChatbotResponse(String keyword, String response) {
        Optional<ChatbotResponse> existingResponse = chatbotResponseRepository.findById(keyword);
        existingResponse.ifPresent(entity -> {
            entity.setResponse(response);
            chatbotResponseRepository.save(entity); // 데이터 저장 (Create & Update)
        });
    }

    public void deleteChatbotResponse(String keyword) {
        chatbotResponseRepository.deleteById(keyword); // Primary Key로 데이터 삭제
    }
}