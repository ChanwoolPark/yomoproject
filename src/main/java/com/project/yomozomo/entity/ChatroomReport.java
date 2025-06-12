package com.project.yomozomo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chatroom_report")
public class ChatroomReport {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_seq_gen")
    @SequenceGenerator(name = "report_seq_gen", sequenceName = "REPORT_SEQ", allocationSize = 1)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "reported_id", nullable = false)
    private Long reportedId;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 1000, nullable = false)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt = new Date();

    @Column(length = 20)
    private String status = "처리중";
}