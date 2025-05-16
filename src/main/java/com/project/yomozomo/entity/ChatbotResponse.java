package com.project.yomozomo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "chatbot_response") // 테이블 이름 명시
public class ChatbotResponse {

    @Id
    @Column(name = "keyword", length = 100) // keyword를 Primary Key로, 길이 명시
    private String keyword;

    @Column(name = "response", length = 4000, nullable = false) // 컬럼 이름, 길이, NOT NULL 명시
    private String response;

    // ✅ 꼭 필요한 getter & setter
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    // id 필드는 Primary Key가 keyword이므로 더 이상 필요하지 않습니다.
}