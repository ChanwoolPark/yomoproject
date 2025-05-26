package com.project.yomozomo.domain;

import com.project.yomozomo.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "product_likes")
@Getter
@Setter
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_product_likes")
    @SequenceGenerator(name = "seq_product_likes", sequenceName = "seq_product_likes", allocationSize = 1)
    @Column(name = "like_id")
    private int likeId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "liked_date")
    private Date likedDate = new Date();
}
