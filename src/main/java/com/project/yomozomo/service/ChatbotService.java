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

    // 새로운 자동 응답 메시지 추가
    public void addChatbotResponse(String keyword, String response) {
        ChatbotResponse newResponse = new ChatbotResponse();
        newResponse.setKeyword(keyword);
        newResponse.setResponse(response);
        chatbotResponseRepository.save(newResponse);
    }

    // 특정 키워드에 대한 자동 응답 메시지 조회
    public Optional<ChatbotResponse> getChatbotResponse(String keyword) {
        return chatbotResponseRepository.findById(keyword);
    }

    // 모든 자동 응답 메시지 조회
    public Iterable<ChatbotResponse> getAllChatbotResponses() {
        return chatbotResponseRepository.findAll();
    }

    // 특정 키워드의 자동 응답 메시지 수정
    public void updateChatbotResponse(String keyword, String response) {
        Optional<ChatbotResponse> existingResponse = chatbotResponseRepository.findById(keyword);
        existingResponse.ifPresent(responseEntity -> {
            responseEntity.setResponse(response);
            chatbotResponseRepository.save(responseEntity);
        });
    }

    // 특정 키워드의 자동 응답 메시지 삭제
    public void deleteChatbotResponse(String keyword) {
        chatbotResponseRepository.deleteById(keyword);
    }

    // ... 기타 필요한 서비스 로직 ...
}