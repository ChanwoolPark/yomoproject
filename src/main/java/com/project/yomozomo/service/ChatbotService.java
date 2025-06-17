package com.project.yomozomo.service;

import com.project.yomozomo.entity.ChatbotAnswer;
import com.project.yomozomo.entity.ChatbotOption;
import com.project.yomozomo.entity.ChatbotKeyword;

import com.project.yomozomo.repository.ChatbotAnswerRepository;
import com.project.yomozomo.repository.ChatbotOptionRepository;
import com.project.yomozomo.repository.ChatbotKeywordRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 관리를 위해 추가

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

    /**
     * 초기 챗봇 메시지를 반환합니다.
     * @return 챗봇 초기 응답 (Map 형태)
     */
    public Map<String, Object> getInitialMessage() {
        // ChatbotAnswer 생성자에 id는 null로 넘겨줍니다. (DB에서 자동 생성)
        ChatbotAnswer initialAnswer = new ChatbotAnswer(null, "안녕하세요 yomozomo 챗봇 상담이에요. 무엇을 도와드릴까요?", null, null, null);
        return createAnswerResponse(initialAnswer);
    }

    /**
     * 사용자의 옵션 선택을 처리하고 응답을 반환합니다.
     * @param optionId 선택된 옵션의 ID
     * @return 챗봇 응답 (Map 형태)
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 설정
    public Map<String, Object> processOptionSelection(Long optionId) {
        Optional<ChatbotOption> optionalOption = optionRepository.findById(optionId);

        if (optionalOption.isPresent()) {
            ChatbotOption option = optionalOption.get();

            // 옵션에 연결된 답변이 있는지 확인
            // ChatbotOption 엔티티가 ChatbotAnswer를 @ManyToOne으로 참조하고 있다면,
            // 기본적으로 LAZY 로딩될 수 있으므로, 해당 필드를 사용할 때 트랜잭션이 필요합니다.
            // 또는 ChatbotOption 엔티티의 answer 필드를 EAGER 로딩으로 변경하거나,
            // ChatbotOption에 answer_id 필드를 직접 가지고 있게 변경하는 것이 더 안전할 수 있습니다.
            // 일단 현재 코드 구조를 유지하며, getAnswer()가 null인지 확실히 체크합니다.
            if (option.getAnswer() != null) {
                return createAnswerResponse(option.getAnswer());
            } else {
                return createErrorResponse("해당 옵션에 연결된 답변을 찾을 수 없습니다.");
            }
        } else {
            return createErrorResponse("유효하지 않은 옵션입니다.");
        }
    }

    /**
     * 사용자 텍스트 입력을 처리하고 응답을 반환합니다.
     * @param userText 사용자 입력 텍스트
     * @return 챗봇 응답 (Map 형태)
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 설정
    public Map<String, Object> processUserText(String userText) {
        String cleanedText = userText.trim();

        // 1. 키워드 기반 답변 검색 (정확한 키워드 매칭)
        // ChatbotKeyword 엔티티가 answerId를 직접 가지고 있다고 가정하고 변경
        Optional<ChatbotKeyword> optionalKeyword = keywordRepository.findByKeyword(cleanedText);

        if (optionalKeyword.isPresent()) {
            Long answerId = optionalKeyword.get().getAnswerId(); // ChatbotKeyword에서 answerId를 가져옴
            Optional<ChatbotAnswer> foundAnswerOpt = answerRepository.findById(answerId); // 해당 ID로 ChatbotAnswer 조회

            if (foundAnswerOpt.isPresent()) {
                // 키워드에 매칭되는 답변을 찾았으므로 반환
                return createAnswerResponse(foundAnswerOpt.get());
            } else {
                // 키워드는 매칭되었으나 연결된 답변이 없는 경우
                return createErrorResponse("매칭된 키워드에 대한 답변을 찾을 수 없습니다.");
            }
        }

        // 2. 연관 키워드 매칭 (부분 일치 또는 유의어 검색)
        // 모든 답변을 가져와 연관 키워드 필드를 기반으로 매칭
        List<ChatbotAnswer> allAnswers = answerRepository.findAll();
        Optional<ChatbotAnswer> foundAnswerByRelatedKeywords = Optional.empty();
        int maxMatchCount = 0;

        for (ChatbotAnswer answer : allAnswers) {
            if (answer.getRelatedKeywords() != null && !answer.getRelatedKeywords().isEmpty()) {
                // 콤마로 구분된 키워드를 소문자로 변환하여 리스트로 만듦
                List<String> keywordsInAnswer = Arrays.asList(answer.getRelatedKeywords().toLowerCase().split(","));
                int currentMatchCount = 0;
                for (String keyword : keywordsInAnswer) {
                    // 사용자 입력 텍스트가 키워드를 포함하는지 확인
                    if (cleanedText.toLowerCase().contains(keyword.trim())) {
                        currentMatchCount++;
                    }
                }
                // 가장 많이 일치하는 답변을 선택
                if (currentMatchCount > maxMatchCount) {
                    maxMatchCount = currentMatchCount;
                    foundAnswerByRelatedKeywords = Optional.of(answer);
                }
            }
        }

        if (foundAnswerByRelatedKeywords.isPresent()) {
            // 연관 키워드에 매칭되는 답변을 찾았으므로 반환
            return createAnswerResponse(foundAnswerByRelatedKeywords.get());
        }

        // 3. 일치하는 키워드가 없을 경우 기본 답변 반환
        return createErrorResponse("죄송합니다. 이해하지 못했습니다. 다른 질문을 해주세요.");
    }

    /**
     * ChatbotAnswer 객체를 Map 형태로 변환하여 일관된 응답을 생성합니다.
     * @param answer ChatbotAnswer 객체
     * @return Map 형태의 응답 데이터
     */
    private Map<String, Object> createAnswerResponse(ChatbotAnswer answer) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "ANSWER");
        response.put("question", null); // 답변 타입 응답에는 질문이 없습니다.

        Map<String, Object> answerMap = new HashMap<>();
        answerMap.put("content", answer.getContent());
        answerMap.put("linkUrl", answer.getLinkUrl());
        answerMap.put("linkText", answer.getLinkText());

        response.put("answer", answerMap);
        return response;
    }

    /**
     * 에러 응답을 쉽게 생성하기 위한 헬퍼 메서드입니다.
     * @param errorMessage 표시할 에러 메시지
     * @return Map 형태의 에러 응답 데이터
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "ERROR");
        response.put("errorMessage", errorMessage);
        return response;
    }
}