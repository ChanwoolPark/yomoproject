package com.project.yomozomo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_image")
@Getter
@Setter
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_product_image")
    @SequenceGenerator(name = "seq_product_image", sequenceName = "seq_product_image", allocationSize = 1)
    @Column(name = "image_id")
    private int imageId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false, length = 200)
    private String imageUrl;


}
