package com.project.yomozomo.entity;

import jakarta.persistence.*; // 또는 javax.persistence.*

@Entity
@Table(name = "chatbot_keyword")
public class ChatbotKeyword {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(nullable = false)
    private String keyword;

    @Column(name = "ANSWER_ID", nullable = false) // DB의 answer_id 컬럼과 매핑
    private Long answerId;


    // 기본 생성자
    public ChatbotKeyword() {}

    // 모든 필드를 포함하는 생성자 (필요하다면)
    public ChatbotKeyword(Long id, String keyword, Long answerId) {
        this.id = id;
        this.keyword = keyword;
        this.answerId = answerId;
    }

    // Getter와 Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getAnswerId() { // answerId에 대한 Getter 추가
        return answerId;
    }

    public void setAnswerId(Long answerId) { // answerId에 대한 Setter 추가
        this.answerId = answerId;
    }
}