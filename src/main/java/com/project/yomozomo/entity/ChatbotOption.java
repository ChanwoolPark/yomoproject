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

    @Column(nullable = false, length = 200)
    private String text; // 선택지 텍스트 (예: "온라인 결제", "환불 문의")

    @Column(nullable = false)
    private int displayOrder; // 옵션 표시 순서

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private ChatbotQuestion question; // 이 옵션이 속한 질문

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_question_id")
    private ChatbotQuestion nextQuestion;

    // OneToOne: 이 옵션이 선택되었을 때, 제공될 최종 답변
    // cascade = CascadeType.ALL, orphanRemoval = true 제거
    @OneToOne(fetch = FetchType.LAZY) // CascadeType.ALL과 orphanRemoval = true를 제거했습니다.
    @JoinColumn(name = "answer_id")
    private ChatbotAnswer answer; // 이 옵션에 대한 최종 답변
}