package com.project.yomozomo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // 다른 사용자 관련 필드 (예: username, password 등)

    // 기본 생성자 및 Getter/Setter
}