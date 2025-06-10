package com.project.yomozomo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Inquiry{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private String email;
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user; // 로그인한 유저 기준 (선택)

    // 답변 내용
    @Column(length = 1000)
    private String answer;

    // 답변 시간
    private LocalDateTime answeredAt;

    @Column
    private Boolean isAnswered = false; // 처리 여부

}
