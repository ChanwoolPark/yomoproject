package com.project.yomozomo.service;

import com.project.yomozomo.entity.ChatbotAnswer;
import com.project.yomozomo.entity.ChatbotOption;
import com.project.yomozomo.entity.ChatbotKeyword;
// import com.project.yomozomo.entity.ChatbotQuestion; // ChatbotQuestion 관련 코드를 사용하지 않으므로 주석 처리하거나 삭제

import com.project.yomozomo.repository.ChatbotAnswerRepository;
import com.project.yomozomo.repository.ChatbotOptionRepository;
import com.project.yomozomo.repository.ChatbotKeywordRepository;
// import com.project.yomozomo.repository.ChatbotQuestionRepository; // ChatbotQuestion 관련 코드를 사용하지 않으므로 주석 처리하거나 삭제

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap; // Map 사용을 위한 import
import java.util.Map;     // Map 사용을 위한 import

@Service
public class ChatbotService {

    private final ChatbotOptionRepository optionRepository;
    private final ChatbotAnswerRepository answerRepository;
    private final ChatbotKeywordRepository keywordRepository;
    // private final ChatbotQuestionRepository questionRepository; // ChatbotQuestion 관련 코드를 사용하지 않으므로 주석 처리하거나 삭제

    @Autowired
    public ChatbotService(
            ChatbotOptionRepository optionRepository,
            ChatbotAnswerRepository answerRepository,
            ChatbotKeywordRepository keywordRepository) {
        // ChatbotQuestionRepository questionRepository) { // ChatbotQuestion 관련 코드를 사용하지 않으므로 주석 처리하거나 삭제
        this.optionRepository = optionRepository;
        this.answerRepository = answerRepository;
        this.keywordRepository = keywordRepository;
        // this.questionRepository = questionRepository; // ChatbotQuestion 관련 코드를 사용하지 않으므로 주석 처리하거나 삭제
    }

    // Map으로 응답 반환
    public Map<String, Object> getInitialMessage() {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "ANSWER"); // 타입을 문자열로 직접 지정
        response.put("question", null); // 질문은 없으므로 null
        ChatbotAnswer initialMessage = new ChatbotAnswer(null, "안녕하세요 yomozomo 챗봇 상담이에요. 무엇을 도와드릴까요?", null);
        response.put("answer", initialMessage); // 답변 엔티티
        return response;
    }

    // Map으로 응답 반환
    public Map<String, Object> processOptionSelection(Long optionId) {
        Map<String, Object> response = new HashMap<>();
        Optional<ChatbotOption> optionalOption = optionRepository.findById(optionId);

        if (optionalOption.isPresent()) {
            ChatbotOption option = optionalOption.get();
            // ChatbotOption 엔티티에 nextQuestion 필드가 있다면 이 로직을 활성화 (현재 ChatbotService에서 관련 로직 없음)
            // if (option.getNextQuestion() != null) {
            //     response.put("type", "QUESTION");
            //     response.put("question", option.getNextQuestion().getContent());
            //     response.put("answer", null);
            // } else
            if (option.getAnswer() != null) { // <<<--- 이 부분을 getAnswer()로 수정
                response.put("type", "ANSWER");
                response.put("question", null);
                response.put("answer", option.getAnswer());
            } else {
                response.put("type", "ERROR"); // 답변이 연결되지 않은 옵션
                response.put("content", "해당 옵션에 연결된 답변이 없습니다."); // 추가적인 에러 메시지
            }
        } else {
            response.put("type", "ERROR");
            response.put("content", "유효하지 않은 옵션입니다."); // 추가적인 에러 메시지
        }
        return response;
    }

    // Map으로 응답 반환
    public Map<String, Object> processUserText(String userText) {
        Map<String, Object> response = new HashMap<>();
        String cleanedText = userText.trim();

        Optional<ChatbotKeyword> optionalKeyword = keywordRepository.findByKeyword(cleanedText);

        if (optionalKeyword.isPresent()) {
            ChatbotAnswer foundAnswerByKeyword = optionalKeyword.get().getAnswer(); // <<<--- 이 줄을 수정합니다.
            response.put("type", "ANSWER");
            response.put("question", null);
            response.put("answer", foundAnswerByKeyword);
            return response;
        }

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
            response.put("answer", foundAnswerByRelatedKeywords.get());
            return response;
        }

        ChatbotAnswer defaultAnswer = new ChatbotAnswer(null, "죄송합니다. 이해하지 못했습니다. 다른 질문을 해주세요.", null);
        response.put("type", "ANSWER"); // 기본 답변도 ANSWER 타입으로 처리
        response.put("question", null);
        response.put("answer", defaultAnswer);
        return response;
    }
}