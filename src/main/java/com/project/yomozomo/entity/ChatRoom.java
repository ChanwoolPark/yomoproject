// src/main/java/com/chat/entity/ChatRoom.java
package com.project.yomozomo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_ROOM") // 실제 테이블명과 일치
@Getter
@Setter
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_room_seq") // 시퀀스 사용 예시
    @SequenceGenerator(name = "chat_room_seq", sequenceName = "CHAT_ROOM_SEQ", allocationSize = 1)
    @Column(name = "ROOM_ID")
    private Long roomId;

    @Column(name = "ROOM_NAME", nullable = false)
    private String roomName;

    @Column(name = "CREATED_AT", columnDefinition = "TIMESTAMP DEFAULT SYSTIMESTAMP")
    private LocalDateTime createdAt; // Oracle의 SYSTIMESTAMP와 매핑

    // 양방향 매핑 (선택 사항) - ChatRoom에 속한 Chat 메시지들을 가져올 때 유용
    // @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Chat> chats = new ArrayList<>();

    @PrePersist // 엔티티 저장 전에 실행
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}