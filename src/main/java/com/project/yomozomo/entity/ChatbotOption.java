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
    @Column(name = "ID") // DB 컬럼명과 매핑
    private Long id;

    @Column(name = "CONTENT", nullable = true, length = 255) // DB 컬럼명과 매핑
    private String content; // 필드명도 DB 컬럼명과 일치시키는 것을 권장 (content로 변경)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QUESTION_ID") // DB 컬럼명과 매핑
    private ChatbotQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NEXT_QUESTION_ID") // DB 컬럼명과 매핑
    private ChatbotQuestion nextQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ANSWER_ID") // DB 컬럼명과 매핑
    private ChatbotAnswer answer;

    @Column(name = "DISPLAY_ORDER", nullable = false) // DB 컬럼명과 매핑
    private Integer displayOrder;
}