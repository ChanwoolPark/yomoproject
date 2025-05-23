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
    private String content; // 답변 내용 (예: "로그인 문제는 다음과 같이 해결할 수 있습니다...")

    @Column(length = 255)
    private String relatedKeywords; // 이 답변과 연관된 키워드 (콤마로 구분)
    // 예: "로그인, 오류, 비밀번호, 계정"
}