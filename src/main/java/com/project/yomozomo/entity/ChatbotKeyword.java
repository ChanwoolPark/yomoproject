package com.project.yomozomo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "chatbot_keyword")
public class ChatbotKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword;

    // 키워드와 연결되는 답변 필드를 추가합니다.
    @ManyToOne // 하나의 답변에 여러 키워드가 연결될 수 있으므로 ManyToOne을 사용합니다.
    @JoinColumn(name = "answer_id") // 데이터베이스 컬럼 이름 (예: answer_id)
    private ChatbotAnswer answer; // 이 필드가 있어야 getAnswer()를 호출할 수 있습니다.

    // 기존에 존재했던 다른 필드나 관계들이 있다면 여기에 계속 유지합니다.
    // 예를 들어, ChatbotQuestion과의 관계:
    // @ManyToOne
    // @JoinColumn(name = "question_id")
    // private ChatbotQuestion relatedQuestion;

    // Constructors, etc.
}