package com.project.yomozomo.service;

import com.project.yomozomo.entity.ChatbotAnswer;
import com.project.yomozomo.entity.ChatbotOption;
import com.project.yomozomo.entity.ChatbotKeyword;

import com.project.yomozomo.repository.ChatbotAnswerRepository;
import com.project.yomozomo.repository.ChatbotOptionRepository;
import com.project.yomozomo.repository.ChatbotKeywordRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class ChatbotService {

    private final ChatbotOptionRepository optionRepository;
    private final ChatbotAnswerRepository answerRepository;
    private final ChatbotKeywordRepository keywordRepository;

    @Autowired
    public ChatbotService(
            ChatbotOptionRepository optionRepository,
            ChatbotAnswerRepository answerRepository,
            ChatbotKeywordRepository keywordRepository) {
        this.optionRepository = optionRepository;
        this.answerRepository = answerRepository;
        this.keywordRepository = keywordRepository;
    }

    // --- getInitialMessage() 메서드 수정 ---
    // Map으로 응답 반환: 초기 메시지에 링크 포함 가능하도록
    public Map<String, Object> getInitialMessage() {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "ANSWER");
        response.put("question", null); // 질문은 없으므로 null

        // ChatbotAnswer 엔티티가 linkUrl과 linkText 필드를 가지고 있다고 가정합니다.
        // 초기 메시지에 링크가 필요하다면 아래처럼 인스턴스 생성 시 값을 넣어주세요.
        // 현재는 링크가 없는 초기 메시지로 설정합니다.
        ChatbotAnswer initialMessage = new ChatbotAnswer(null, "안녕하세요 yomozomo 챗봇 상담이에요. 무엇을 도와드릴까요?", null, null, null); // ChatbotAnswer 생성자 변경 가정

        // ChatbotAnswer 객체를 Map 형태로 변환하여 반환
        Map<String, Object> answerMap = new HashMap<>();
        answerMap.put("content", initialMessage.getContent());
        answerMap.put("linkUrl", initialMessage.getLinkUrl()); // ChatbotAnswer에 getLinkUrl() 메서드 필요
        answerMap.put("linkText", initialMessage.getLinkText()); // ChatbotAnswer에 getLinkText() 메서드 필요

        response.put("answer", answerMap);
        return response;
    }

    // --- processOptionSelection() 메서드 수정 ---
    // Map으로 응답 반환: 옵션 선택 시 링크 포함 가능하도록
    public Map<String, Object> processOptionSelection(Long optionId) {
        Map<String, Object> response = new HashMap<>();
        Optional<ChatbotOption> optionalOption = optionRepository.findById(optionId);

        if (optionalOption.isPresent()) {
            ChatbotOption option = optionalOption.get();

            if (option.getAnswer() != null) {
                response.put("type", "ANSWER");
                response.put("question", null);

                // ChatbotAnswer 객체를 Map 형태로 변환하여 반환
                ChatbotAnswer chatbotAnswer = option.getAnswer();
                Map<String, Object> answerMap = new HashMap<>();
                answerMap.put("content", chatbotAnswer.getContent());
                answerMap.put("linkUrl", chatbotAnswer.getLinkUrl()); // ChatbotAnswer에 getLinkUrl() 메서드 필요
                answerMap.put("linkText", chatbotAnswer.getLinkText()); // ChatbotAnswer에 getLinkText() 메서드 필요

                response.put("answer", answerMap);
            } else {
                response.put("type", "ERROR"); // 답변이 연결되지 않은 옵션
                response.put("errorMessage", "해당 옵션에 연결된 답변이 없습니다."); // 클라이언트에서 errorMessage 필드를 기대하므로 수정
            }
        } else {
            response.put("type", "ERROR");
            response.put("errorMessage", "유효하지 않은 옵션입니다."); // 클라이언트에서 errorMessage 필드를 기대하므로 수정
        }
        return response;
    }

    // --- processUserText() 메서드 수정 ---
    // Map으로 응답 반환: 사용자 텍스트 처리 시 링크 포함 가능하도록
    public Map<String, Object> processUserText(String userText) {
        Map<String, Object> response = new HashMap<>();
        String cleanedText = userText.trim();

        // 1. 키워드 매칭
        Optional<ChatbotKeyword> optionalKeyword = keywordRepository.findByKeyword(cleanedText);

        if (optionalKeyword.isPresent()) {
            ChatbotAnswer foundAnswerByKeyword = optionalKeyword.get().getAnswer();
            response.put("type", "ANSWER");
            response.put("question", null);

            // ChatbotAnswer 객체를 Map 형태로 변환하여 반환
            Map<String, Object> answerMap = new HashMap<>();
            answerMap.put("content", foundAnswerByKeyword.getContent());
            answerMap.put("linkUrl", foundAnswerByKeyword.getLinkUrl()); // ChatbotAnswer에 getLinkUrl() 메서드 필요
            answerMap.put("linkText", foundAnswerByKeyword.getLinkText()); // ChatbotAnswer에 getLinkText() 메서드 필요

            response.put("answer", answerMap);
            return response;
        }

        // 2. 연관 키워드 매칭
        List<ChatbotAnswer> allAnswers = answerRepository.findAll();
        Optional<ChatbotAnswer> foundAnswerByRelatedKeywords = Optional.empty();
        int maxMatchCount = 0;

        for (ChatbotAnswer answer : allAnswers) {
            if (answer.getRelatedKeywords() != null && !answer.getRelatedKeywords().isEmpty()) {
                List<String> keywordsInAnswer = Arrays.asList(answer.getRelatedKeywords().toLowerCase().split(","));
                int currentMatchCount = 0;
                for (String keyword : keywordsInAnswer) {
                    if (cleanedText.toLowerCase().contains(keyword.trim())) {
                        currentMatchCount++;
                    }
                }
                if (currentMatchCount > maxMatchCount) {
                    maxMatchCount = currentMatchCount;
                    foundAnswerByRelatedKeywords = Optional.of(answer);
                }
            }
        }

        if (foundAnswerByRelatedKeywords.isPresent()) {
            response.put("type", "ANSWER");
            response.put("question", null);

            // ChatbotAnswer 객체를 Map 형태로 변환하여 반환
            ChatbotAnswer chatbotAnswer = foundAnswerByRelatedKeywords.get();
            Map<String, Object> answerMap = new HashMap<>();
            answerMap.put("content", chatbotAnswer.getContent());
            answerMap.put("linkUrl", chatbotAnswer.getLinkUrl()); // ChatbotAnswer에 getLinkUrl() 메서드 필요
            answerMap.put("linkText", chatbotAnswer.getLinkText()); // ChatbotAnswer에 getLinkText() 메서드 필요

            response.put("answer", answerMap);
            return response;
        }

        // 3. 일치하는 키워드가 없을 경우 기본 답변
        ChatbotAnswer defaultAnswer = new ChatbotAnswer(null, "죄송합니다. 이해하지 못했습니다. 다른 질문을 해주세요.", null, null, null); // ChatbotAnswer 생성자 변경 가정
        response.put("type", "ANSWER"); // 기본 답변도 ANSWER 타입으로 처리
        response.put("question", null);

        // ChatbotAnswer 객체를 Map 형태로 변환하여 반환
        Map<String, Object> answerMap = new HashMap<>();
        answerMap.put("content", defaultAnswer.getContent());
        answerMap.put("linkUrl", defaultAnswer.getLinkUrl()); // ChatbotAnswer에 getLinkUrl() 메서드 필요
        answerMap.put("linkText", defaultAnswer.getLinkText()); // ChatbotAnswer에 getLinkText() 메서드 필요

        response.put("answer", answerMap);
        return response;
    }
}