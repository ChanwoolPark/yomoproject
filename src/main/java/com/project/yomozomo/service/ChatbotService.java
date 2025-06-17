// src/main/java/com/project/yomozomo/service/ChatbotService.java (가정)
package com.project.yomozomo.service;

import com.project.yomozomo.dto.ChatbotResponse;
import com.project.yomozomo.entity.ChatbotAnswer;
import com.project.yomozomo.entity.ChatbotKeyword;
import com.project.yomozomo.entity.ChatbotOption;
import com.project.yomozomo.entity.ChatbotQuestion;
import com.project.yomozomo.repository.ChatbotAnswerRepository;
import com.project.yomozomo.repository.ChatbotKeywordRepository;
import com.project.yomozomo.repository.ChatbotOptionRepository;
import com.project.yomozomo.repository.ChatbotQuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Collectors 추가

@Service
public class ChatbotService {

    private final ChatbotQuestionRepository chatbotQuestionRepository;
    private final ChatbotAnswerRepository chatbotAnswerRepository;
    private final ChatbotOptionRepository chatbotOptionRepository;
    private final ChatbotKeywordRepository chatbotKeywordRepository;

    // 초기 질문 ID (DML 스크립트에서 1번으로 설정됨)
    private static final Long INITIAL_QUESTION_ID = 1L;

    public ChatbotService(ChatbotQuestionRepository chatbotQuestionRepository,
                          ChatbotAnswerRepository chatbotAnswerRepository,
                          ChatbotOptionRepository chatbotOptionRepository,
                          ChatbotKeywordRepository chatbotKeywordRepository) {
        this.chatbotQuestionRepository = chatbotQuestionRepository;
        this.chatbotAnswerRepository = chatbotAnswerRepository;
        this.chatbotOptionRepository = chatbotOptionRepository;
        this.chatbotKeywordRepository = chatbotKeywordRepository;
    }

    // 챗봇 시작 시 초기 메시지 반환
    public ChatbotResponse getInitialMessage() {
        Optional<ChatbotQuestion> initialQuestionOpt = chatbotQuestionRepository.findById(INITIAL_QUESTION_ID);
        if (initialQuestionOpt.isPresent()) {
            ChatbotQuestion initialQuestion = initialQuestionOpt.get();
            List<String> suggestedKeywords = getSuggestedKeywordsFromInitialOptions(); // 초기 옵션에서 키워드 가져오기
            return new ChatbotResponse("QUESTION", null, initialQuestion.getContent(), null, suggestedKeywords);
        }
        return new ChatbotResponse("ERROR", null, null, "챗봇을 시작할 수 없습니다.", null);
    }

    // 사용자 메시지 처리
    public ChatbotResponse processMessage(String message) {
        // 1. 키워드 매칭 시도
        Optional<ChatbotKeyword> matchedKeywordOpt = chatbotKeywordRepository.findByKeyword(message);

        if (matchedKeywordOpt.isPresent()) {
            ChatbotKeyword matchedKeyword = matchedKeywordOpt.get();
            Optional<ChatbotAnswer> answerOpt = chatbotAnswerRepository.findById(matchedKeyword.getAnswerId());
            if (answerOpt.isPresent()) {
                // 키워드에 매칭되면 답변 반환 (이때는 추천 키워드 없을 수 있음, 혹은 다음 단계 유도 키워드)
                return new ChatbotResponse("ANSWER", answerOpt.get(), null, null, null);
            }
        }

        // 2. 옵션 매칭 시도 (현재는 메시지에 옵션 텍스트를 직접 입력하는 방식)
        // 예를 들어, "상품 문의", "주문/결제 문의" 등을 직접 입력했을 때
        Optional<ChatbotOption> matchedOptionOpt = chatbotOptionRepository.findByContent(message);
        if (matchedOptionOpt.isPresent()) {
            ChatbotOption matchedOption = matchedOptionOpt.get();
            if (matchedOption.getNextQuestion() != null) {
                // 다음 질문으로 이동
                List<String> suggestedKeywords = getSuggestedKeywordsFromQuestionOptions(matchedOption.getNextQuestion().getId());
                return new ChatbotResponse("QUESTION", null, matchedOption.getNextQuestion().getContent(), null, suggestedKeywords);
            } else if (matchedOption.getAnswer() != null) {
                // 최종 답변으로 이동
                // 답변 후 추천 키워드 제공 (예: 다시 초기 질문 옵션 추천)
                List<String> suggestedKeywords = getSuggestedKeywordsFromInitialOptions();
                return new ChatbotResponse("ANSWER", matchedOption.getAnswer(), null, null, suggestedKeywords);
            }
        }

        // 3. 어떤 것도 매칭되지 않았을 경우 (질문을 잘못했을 때)
        List<String> suggestedKeywords = getSuggestedKeywordsFromInitialOptions(); // 초기 옵션 키워드 추천
        return new ChatbotResponse("ERROR", null, null, "죄송합니다. 이해하지 못했습니다. 다음 키워드를 이용해 보세요.", suggestedKeywords);
    }

    // 헬퍼 메서드: 초기 질문의 옵션들을 추천 키워드로 가져오기
    private List<String> getSuggestedKeywordsFromInitialOptions() {
        return chatbotOptionRepository.findByQuestionId(INITIAL_QUESTION_ID).stream()
                .map(ChatbotOption::getContent)
                .collect(Collectors.toList());
    }

    // 헬퍼 메서드: 특정 질문 ID에 해당하는 옵션들을 추천 키워드로 가져오기
    private List<String> getSuggestedKeywordsFromQuestionOptions(Long questionId) {
        return chatbotOptionRepository.findByQuestionId(questionId).stream()
                .map(ChatbotOption::getContent)
                .collect(Collectors.toList());
    }
}