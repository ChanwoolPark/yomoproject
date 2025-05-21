// src/main/java/com/project/yomozomo/entity/ChatbotAnswer.java
package com.project.yomozomo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "chatbot_answer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content; // 최종 답변 내용 (예: "환불은 마이페이지에서 신청하실 수 있습니다.")
}