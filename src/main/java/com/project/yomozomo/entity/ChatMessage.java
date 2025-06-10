// src/main/java/com/project/yomozomo/entity/ChatMessage.java

package com.project.yomozomo.entity;

import jakarta.persistence.*; // JPA 관련 어노테이션 임포트 확인
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT") // ⭐ 테이블 이름을 'CHAT'으로 명시 ⭐
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage { // 엔티티 클래스 이름은 ChatMessage로 유지

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_seq_generator") // Oracle SEQUENCE 사용
    @SequenceGenerator(name = "chat_seq_generator", sequenceName = "CHAT_SEQ", allocationSize = 1) // ⭐ Oracle Sequence 이름 확인 필요 ⭐
    @Column(name = "CHAT_ID") // ⭐ DB 컬럼명 CHAT_ID와 매핑 ⭐
    private Long messageId; // 엔티티 필드명은 messageId로 유지

    @Column(name = "ROOM_ID", nullable = false) // ⭐ DB 컬럼명 ROOM_ID와 매핑 ⭐
    private Long roomId;

    @Column(name = "USER_ID") // ⭐ DB 컬럼명 USER_ID와 매핑 (DB 스키마에 따라 nullable 여부 확인) ⭐
    private Long senderId; // 엔티티 필드명은 senderId로 유지 (의미상 sender_id가 user_id이므로)

    @Lob // CLOB 타입 매핑
    @Column(name = "MESSAGE") // ⭐ DB 컬럼명 MESSAGE와 매핑 ⭐
    private String message;

    @Column(name = "IMG_URL", length = 500) // ⭐ DB 컬럼명 IMG_URL과 매핑 ⭐
    private String imgUrl;

    // HAS_IMAGE 컬럼 추가 (CHAR(1) 'Y'/'N' 매핑)
    @Column(name = "HAS_IMAGE", length = 1)
    private String hasImage = "N"; // 기본값 'N'

    @Column(name = "MESSAGE_TYPE", length = 20) // ⭐ DB 컬럼명 MESSAGE_TYPE과 매핑 ⭐
    private String messageType = "TEXT"; // 기본값 'TEXT'

    @Column(name = "CREATED_AT") // ⭐ DB 컬럼명 CREATED_AT와 매핑 ⭐
    private LocalDateTime sendTime; // 엔티티 필드명은 sendTime으로 유지 (의미상 생성 시간이므로)

    @PrePersist
    protected void onCreate() {
        if (sendTime == null) {
            sendTime = LocalDateTime.now(); // 생성 시간 자동 설정
        }
        if (messageType == null || messageType.isEmpty()) {
            messageType = "TEXT"; // 기본 메시지 타입 설정
        }
        // imgUrl이 있다면 hasImage를 'Y'로 설정
        if (imgUrl != null && !imgUrl.isEmpty()) {
            hasImage = "Y";
        } else {
            hasImage = "N";
        }
    }
}