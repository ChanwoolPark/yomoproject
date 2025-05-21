// src/main/java/com/project/yomozomo/service/ChatbotService.java
package com.project.yomozomo.service;

import com.project.yomozomo.entity.ChatbotAnswer;
import com.project.yomozomo.entity.ChatbotOption;
import com.project.yomozomo.entity.ChatbotQuestion;
import com.project.yomozomo.repository.ChatbotAnswerRepository;
import com.project.yomozomo.repository.ChatbotOptionRepository;
import com.project.yomozomo.repository.ChatbotQuestionRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List; // 추가
import java.util.Arrays; // 추가

@Service
public class ChatbotService {

    private final ChatbotQuestionRepository questionRepository;
    private final ChatbotOptionRepository optionRepository;
    private final ChatbotAnswerRepository answerRepository;

    @Autowired
    public ChatbotService(ChatbotQuestionRepository questionRepository,
                          ChatbotOptionRepository optionRepository,
                          ChatbotAnswerRepository answerRepository) {
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.answerRepository = answerRepository;
    }

    // 챗봇 초기 질문 (ID=1L로 가정)
    public Optional<ChatbotQuestion> getInitialQuestion() {
        return questionRepository.findById(1L); // 첫 질문은 ID 1L로 가정합니다.
    }

    // 옵션 선택 처리 (이전 방식, 여전히 필요할 수 있음)
    public ChatbotResponse processOptionSelection(Long optionId) {
        Optional<ChatbotOption> optionalOption = optionRepository.findById(optionId);
        if (optionalOption.isPresent()) {
            ChatbotOption option = optionalOption.get();
            if (option.getNextQuestion() != null) {
                return new ChatbotResponse(ChatbotResponseType.QUESTION, option.getNextQuestion(), null);
            } else if (option.getAnswer() != null) {
                return new ChatbotResponse(ChatbotResponseType.ANSWER, null, option.getAnswer());
            }
        }
        return new ChatbotResponse(ChatbotResponseType.ERROR, null, null); // 옵션이 유효하지 않을 경우
    }

    // --- 새로운 사용자 텍스트 입력 처리 로직 ---
    public ChatbotResponse processUserText(String userText) {
        // 텍스트를 소문자로 변환하고 공백 제거 (검색 정확도 향상)
        String cleanedText = userText.trim().toLowerCase();

        // 1. ChatbotAnswer의 relatedKeywords를 사용하여 가장 적합한 답변 찾기
        List<ChatbotAnswer> allAnswers = answerRepository.findAll();
        Optional<ChatbotAnswer> foundAnswer = Optional.empty();
        int maxMatchCount = 0;

        for (ChatbotAnswer answer : allAnswers) {
            if (answer.getRelatedKeywords() != null && !answer.getRelatedKeywords().isEmpty()) {
                List<String> keywords = Arrays.asList(answer.getRelatedKeywords().toLowerCase().split(","));
                int currentMatchCount = 0;
                for (String keyword : keywords) {
                    if (cleanedText.contains(keyword.trim())) {
                        currentMatchCount++;
                    }
                }
                if (currentMatchCount > maxMatchCount) {
                    maxMatchCount = currentMatchCount;
                    foundAnswer = Optional.of(answer);
                }
            }
        }

        if (foundAnswer.isPresent()) {
            return new ChatbotResponse(ChatbotResponseType.ANSWER, null, foundAnswer.get());
        } else {
            // 일치하는 답변이 없을 경우 기본 질문 또는 오류 메시지 반환
            // 여기서는 기본적으로 '이해하지 못했습니다' 답변을 제공합니다.
            ChatbotAnswer defaultAnswer = new ChatbotAnswer(null, "죄송합니다. 이해하지 못했습니다. 다른 질문을 해주세요.", null);
            return new ChatbotResponse(ChatbotResponseType.ANSWER, null, defaultAnswer);
        }
    }


    // 응답 타입을 위한 Enum 및 내부 클래스
    public enum ChatbotResponseType {
        QUESTION, ANSWER, ERROR
    }

    @Getter
    public static class ChatbotResponse {
        private final ChatbotResponseType type;
        private final ChatbotQuestion question;
        private final ChatbotAnswer answer;

        public ChatbotResponse(ChatbotResponseType type, ChatbotQuestion question, ChatbotAnswer answer) {
            this.type = type;
            this.question = question;
            this.answer = answer;
        }
    }
}