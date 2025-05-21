// src/main/java/com/project/yomozomo/entity/ChatbotOption.java
package com.project.yomozomo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "chatbot_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String text; // 옵션 텍스트 (예: "온라인 결제")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id") // 이 옵션이 속한 질문
    private ChatbotQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_question_id") // 이 옵션을 선택했을 때 연결될 다음 질문
    private ChatbotQuestion nextQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id") // 이 옵션을 선택했을 때 연결될 최종 답변
    private ChatbotAnswer answer;

    @Column(nullable = false)
    private Integer displayOrder; // 옵션 표시 순서
}