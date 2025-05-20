package com.project.yomozomo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity // 이 클래스가 데이터베이스 테이블과 매핑되는 엔티티임을 나타냅니다.
public class ChatbotResponse {

    @Id // `keyword` 컬럼이 Primary Key임을 나타냅니다.
    private String keyword;

    @Column(columnDefinition = "CLOB") // `response` 컬럼의 데이터 타입을 CLOB으로 설정합니다.
    private String response;

    // 기본 생성자 (JPA는 기본 생성자가 필요합니다.)
    public ChatbotResponse() {
    }

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
}