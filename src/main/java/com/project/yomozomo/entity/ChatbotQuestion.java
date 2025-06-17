// src/main/java/com/project/yomozomo/entity/ChatbotQuestion.java
package com.project.yomozomo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "chatbot_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotQuestion {
    @Id
    @Column(name = "QUESTION_ID") // <-- 이 부분을 추가해야 합니다.
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ChatbotOption> options;
}