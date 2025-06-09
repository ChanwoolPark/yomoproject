package com.project.yomozomo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_MESSAGE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_chat_message")
    @SequenceGenerator(name = "seq_chat_message", sequenceName = "SEQ_CHAT_MESSAGE", allocationSize = 1)
    @Column(name = "MESSAGE_ID")
    private Long messageId;

    @Column(name = "ROOM_ID", nullable = false)
    private Long roomId; // Long 타입 그대로 유지

    @Column(name = "SENDER_ID", nullable = false)
    private Long senderId; // Long 타입 그대로 유지 (User 엔티티의 ID)

    @Column(name = "MESSAGE_CONTENT", length = 4000)
    private String message; // 메시지 내용

    @Column(name = "SEND_TIME", columnDefinition = "TIMESTAMP DEFAULT SYSTIMESTAMP")
    private LocalDateTime sendTime;

    @Column(name = "MESSAGE_TYPE", length = 10, nullable = false)
    private String messageType; // 메시지 타입 (TALK, JOIN, LEAVE 등)

    // ⭐ 추가: 웹소켓 DTO와 호환성을 위해 MessageType enum을 엔티티 내부에 정의하거나 별도 파일로 관리 ⭐
    public enum MessageType {
        CHAT, JOIN, LEAVE // 웹소켓 DTO의 enum 값과 일치해야 함
    }
}