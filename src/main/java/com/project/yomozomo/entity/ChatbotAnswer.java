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
    @Column(name = "ANSWER_ID") // DB 컬럼명과 매핑
    private Long id;

    @Column(name = "CONTENT", nullable = false, length = 1000) // DB 컬럼명과 매핑
    private String content;

    @Column(name = "RELATED_KEYWORDS", length = 255) // DB 컬럼명과 매핑
    private String relatedKeywords;

    @Column(name = "LINK_URL", length = 500) // DB 컬럼명과 매핑
    private String linkUrl;

    @Column(name = "LINK_TEXT", length = 100) // DB 컬럼명과 매핑
    private String linkText;
}