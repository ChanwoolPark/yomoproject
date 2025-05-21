package com.project.yomozomo.entity;
import com.project.yomozomo.entity.Chat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "users")
@Getter
@Setter
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "users_seq", sequenceName = "USERS_SEQ", allocationSize = 1)
    @Column(name = "USER_ID")
    private Long userId;
    @Column(name = "USERNAME", nullable = false, unique = true)
    private String username;

    // 다른 사용자 관련 필드 (예: username, password 등)

    // 기본 생성자 및 Getter/Setter
}