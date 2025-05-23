// src/main/java/com/project/yomozomo/entity/ChatbotQuestion.java
package com.project.yomozomo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List; // 옵션 목록을 위해 추가

@Entity
@Table(name = "chatbot_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content; // 질문 내용 (예: "토스에서 진행한 결제에 대해 궁금하신 내용을 선택해주세요.")

    // 한 질문은 여러 옵션을 가질 수 있습니다 (1:N 관계)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    // mappedBy: ChatbotOption 엔티티의 'question' 필드에 의해 매핑됨을 나타냅니다.
    // cascade = CascadeType.ALL: Question 삭제 시 하위 Option도 함께 삭제됩니다.
    // orphanRemoval = true: Question과 연결이 끊어진 Option이 자동으로 삭제됩니다.
    @OrderBy("displayOrder ASC") // 옵션 표시 순서 정렬 (옵션 엔티티에 displayOrder 필드 추가 필요)
    private List<ChatbotOption> options; // 해당 질문에 대한 선택지 목록
}