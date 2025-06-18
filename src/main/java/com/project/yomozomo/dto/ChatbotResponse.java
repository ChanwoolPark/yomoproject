// src/main/java/com/project/yomozomo/dto/ChatbotResponse.java
package com.project.yomozomo.dto;

// 더 이상 com.project.yomozomo.entity.ChatbotAnswer를 직접 임포트하지 않습니다.
// 대신 com.project.yomozomo.dto.ChatbotAnswerResponseDto를 임포트합니다.
// import com.project.yomozomo.entity.ChatbotAnswer; // 이 줄을 삭제하거나 주석 처리하세요.

import lombok.Getter;
import lombok.Setter;
// Lombok의 @AllArgsConstructor는 아래와 같이 사용자 정의 생성자를 사용할 경우
// 때로는 제거하거나 명시적으로 정의하는 것이 좋습니다.
// @AllArgsConstructor를 유지하려면, 아래 사용자 정의 생성자와 함께 명확히 구분해야 합니다.

import java.util.List;

@Getter
@Setter
// @AllArgsConstructor // 이 줄을 제거하거나, 아래 생성자들을 수동으로 명확하게 정의해야 합니다.
public class ChatbotResponse {
    private String type; // "ANSWER", "QUESTION", "ERROR"

    // 이전에 ChatbotAnswer answer; 였던 부분을 아래와 같이 변경해야 합니다.
    private ChatbotAnswerResponseDto answer; // <--- 이 필드를 ChatbotAnswerResponseDto 타입으로 변경해야 합니다!

    private String question; // type이 QUESTION일 경우 (질문 내용 문자열)
    private String errorMessage; // type이 ERROR일 경우
    private List<String> suggestedKeywords; // 추천 키워드 목록 (String 리스트)

    // === 중요: 생성자들을 올바르게 구현해야 합니다 ===

    // ANSWER 타입 응답을 위한 생성자 (ChatbotAnswer 엔티티를 받아서 DTO로 변환)
    public ChatbotResponse(String type, com.project.yomozomo.entity.ChatbotAnswer entityAnswer) {
        this.type = type;
        if (entityAnswer != null) {
            // 여기에서 엔티티를 ChatbotAnswerResponseDto로 변환합니다.
            this.answer = new ChatbotAnswerResponseDto(entityAnswer);
        } else {
            this.answer = null;
        }
        this.question = null; // 나머지 필드는 해당 타입에 맞게 초기화
        this.errorMessage = null;
        this.suggestedKeywords = null;
    }

    // QUESTION 타입 응답을 위한 생성자
    public ChatbotResponse(String type, String question, List<String> suggestedKeywords) {
        this.type = type;
        this.question = question;
        this.suggestedKeywords = suggestedKeywords;
        this.answer = null;
        this.errorMessage = null;
    }

    // ERROR 타입 응답을 위한 생성자
    public ChatbotResponse(String type, String errorMessage) {
        this.type = type;
        this.errorMessage = errorMessage;
        this.answer = null;
        this.question = null;
        this.suggestedKeywords = null;
    }

    // (필요하다면) 모든 필드를 DTO 타입으로 직접 받는 생성자
    public ChatbotResponse(String type, ChatbotAnswerResponseDto answer, String question, String errorMessage, List<String> suggestedKeywords) {
        this.type = type;
        this.answer = answer;
        this.question = question;
        this.errorMessage = errorMessage;
        this.suggestedKeywords = suggestedKeywords;
    }

    // 기본 생성자 (Jackson deserialization에 필요할 수 있음)
    public ChatbotResponse() {
        // 필드를 적절히 초기화
    }
}