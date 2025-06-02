// src/main/java/com/chat/entity/ChatRoom.java
package com.project.yomozomo.entity;

import lombok.Builder;
import com.project.yomozomo.domain.Rental;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor; // 필요하다면 추가
import lombok.AllArgsConstructor; // 필요하다면 추가
import jakarta.persistence.*; // JPA 관련 import

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_ROOM") // 실제 테이블명과 일치
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_chat_room") // 시퀀스 사용 예시
    @SequenceGenerator(name = "seq_chat_room", sequenceName = "seq_chat_room", allocationSize = 1)
    @Column(name = "ROOM_ID")
    private Long roomId;

    @Column(name = "ROOM_NAME",length = 500, nullable = false)
    private String roomName;

    @Column(name = "CREATED_AT", columnDefinition = "TIMESTAMP DEFAULT SYSTIMESTAMP")
    private LocalDateTime createdAt; // Oracle의 SYSTIMESTAMP와 매핑

    @ManyToOne
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

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