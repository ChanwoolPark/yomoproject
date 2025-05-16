package com.project.yomozomo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String role; // user 또는 bot
    private String message;
    private LocalDateTime timestamp;

    public ChatMessage() {}

    public ChatMessage(String role, String message) {
        this.role = role;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // getter/setter 생략
}
