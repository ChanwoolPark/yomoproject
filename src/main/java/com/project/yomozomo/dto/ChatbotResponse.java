// src/main/java/com/project/yomozomo/dto/ChatbotResponse.java
package com.project.yomozomo.dto;

import com.project.yomozomo.entity.ChatbotAnswer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List; // List import 추가

@Getter
@Setter
@AllArgsConstructor
public class ChatbotResponse {
    private String type; // "ANSWER", "QUESTION", "ERROR"
    private ChatbotAnswer answer; // type이 ANSWER일 경우
    private String question; // type이 QUESTION일 경우 (질문 내용 문자열)
    private String errorMessage; // type이 ERROR일 경우
    private List<String> suggestedKeywords; // <-- 추가: 추천 키워드 목록 (String 리스트)
}