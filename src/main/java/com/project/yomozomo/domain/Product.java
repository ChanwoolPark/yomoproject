package com.project.yomozomo.domain;


import com.project.yomozomo.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "product")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_product")
    @SequenceGenerator(name = "seq_product", sequenceName = "seq_product", allocationSize = 1)
    @Column(name = "product_id")
    private int productId;

    // seller는 users 테이블과 연관관계 설정
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne
    @JoinColumn(name = "sub_category_id", nullable = false)
    private SubCategory subCategory;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer deposit = 0;

    @Column(nullable = false)
    private Integer count;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "is_deleted", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String isDeleted = "N";

    @Column(length = 20)
    private String status = "판매중";
}