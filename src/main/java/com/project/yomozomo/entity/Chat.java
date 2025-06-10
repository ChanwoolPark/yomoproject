// src/main/java/com/chat/entity/Chat.java
package com.project.yomozomo.entity;

import com.project.yomozomo.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "CHAT") // 테이블명은 'chat'으로 되어 있으니 그대로 사용
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_seq") // 시퀀스 사용 예시
    @SequenceGenerator(name = "chat_seq", sequenceName = "CHAT_SEQ", allocationSize = 1)
    @Column(name = "CHAT_ID")
    private Long chatId;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩
    @JoinColumn(name = "ROOM_ID", nullable = false) // room_id 컬럼과 매핑
    private com.project.yomozomo.entity.ChatRoom chatRoom; // ChatRoom 엔티티와 관계 설정

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩
    @JoinColumn(name = "USER_ID") // user_id 컬럼과 매핑
    private User user; // Users 엔티티와 관계 설정

    @Lob // CLOB 타입 매핑
    @Column(name = "MESSAGE")
    private String message;

    @Column(name = "IMG_URL", length = 500)
    private String imgUrl;

    @Column(name = "HAS_IMAGE", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private Character hasImage;

    @Column(name = "MESSAGE_TYPE", length = 20, columnDefinition = "VARCHAR2(20) DEFAULT 'TEXT'")
    private String messageType;

    @Column(name = "CREATED_AT", columnDefinition = "TIMESTAMP DEFAULT SYSTIMESTAMP")
    private LocalDateTime createdAt;

    @PrePersist // 엔티티 저장 전에 실행
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.hasImage == null) {
            this.hasImage = 'N';
        }
        if (this.messageType == null) {
            this.messageType = "TEXT";
        }
    }
}