// src/main/java/com/project/yomozomo/dto/ChatbotAnswerDTO.java (예시 경로)
package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lombok 어노테이션을 사용하여 보일러플레이트 코드 줄이기
@Data // Getter, Setter, equals, hashCode, toString 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 포함하는 생성자 자동 생성
public class ChatbotAnswerDTO {
    private Long id;
    private String content;
    private String relatedKeywords;

    // 엔티티를 DTO로 변환하는 정적 팩토리 메서드 (선택 사항이지만 유용)
    public static ChatbotAnswerDTO fromEntity(com.project.yomozomo.entity.ChatbotAnswer entity) {
        if (entity == null) {
            return null;
        }
        return new ChatbotAnswerDTO(entity.getId(), entity.getContent(), entity.getRelatedKeywords());
    }
}