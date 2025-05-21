package com.project.yomozomo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column; // @Column 어노테이션을 사용한다면 필요

import lombok.Getter; // Lombok 사용 시
import lombok.Setter; // Lombok 사용 시
import lombok.NoArgsConstructor; // Lombok 사용 시
import lombok.AllArgsConstructor; // Lombok 사용 시

@Entity // JPA 엔티티임을 명시
@Getter // Lombok: 모든 필드의 Getter 메서드를 자동 생성
@Setter // Lombok: 모든 필드의 Setter 메서드를 자동 생성
@NoArgsConstructor // Lombok: 기본 생성자 자동 생성
@AllArgsConstructor // Lombok: 모든 필드를 인자로 받는 생성자 자동 생성
public class ChatbotKeyword {

    @Id // 기본 키(Primary Key)임을 명시
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 자동 생성 전략
    private Long id;

    @Column(nullable = false, unique = true) // 데이터베이스 컬럼 설정: null 불허, 고유 값
    private String keyword;

    // !!!!! 이 부분이 핵심입니다. answer_id 필드를 추가합니다. !!!!!
    @Column(name = "answer_id", nullable = false) // 데이터베이스 컬럼명 지정 (snake_case)
    private Long answerId; // ChatbotAnswer 엔티티의 ID와 연결될 필드

    // Lombok을 사용하지 않는 경우, 아래와 같이 Getter를 수동으로 추가해야 합니다.
    /*
    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }
    */
}