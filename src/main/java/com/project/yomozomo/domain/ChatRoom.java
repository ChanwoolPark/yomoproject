package com.project.yomozomo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "chat_room",
        uniqueConstraints = @UniqueConstraint(columnNames = {"buyer_id", "seller_id", "rental_id"}))
@Getter
@Setter
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_chat_room")
    @SequenceGenerator(name = "seq_chat_room", sequenceName = "seq_chat_room", allocationSize = 1)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name", length = 500, nullable = false)
    private String roomName;

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;
}