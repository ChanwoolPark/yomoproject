package com.project.yomozomo.dto; // DTO 패키지는 다를 수 있습니다.

import com.project.yomozomo.entity.ChatbotAnswer;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotAnswerResponseDto {
    private Long id;
    private String content;
    private List<String> keywords;
    private String linkUrl;
    private String linkText;

    // ChatbotAnswer 엔티티를 받아서 DTO로 변환하는 생성자 또는 정적 팩토리 메서드
    public ChatbotAnswerResponseDto(ChatbotAnswer answer) {
        this.id = answer.getId();
        this.content = answer.getContent();
        this.linkUrl = answer.getLinkUrl();
        this.linkText = answer.getLinkText();
        // relatedKeywords 문자열을 파싱하여 List<String>으로 변환
        if (answer.getRelatedKeywords() != null && !answer.getRelatedKeywords().trim().isEmpty()) {
            this.keywords = Arrays.stream(answer.getRelatedKeywords().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        } else {
            this.keywords = List.of(); // 키워드가 없으면 빈 리스트 반환
        }
    }
}