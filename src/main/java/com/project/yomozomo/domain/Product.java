package com.project.yomozomo.domain;


import com.project.yomozomo.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_product")
    @SequenceGenerator(name = "seq_product", sequenceName = "seq_product", allocationSize = 1)
    @Column(name = "product_id")
    private Long productId;

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
    private Integer count = 0;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Column(name = "is_deleted", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String isDeleted = "N";

    @Column(length = 20)
    private String status = "판매중";


    public void delete() {
        this.isDeleted = "Y";
    }

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductImage> productImages = new ArrayList<>();

    // 헬퍼 메서드
    public String getThumbnailUrl() {
        if (productImages != null && !productImages.isEmpty()) {
            return productImages.get(0).getImageUrl();
        }
        return "/img/default.png"; // 기본 이미지 경로
    }

}