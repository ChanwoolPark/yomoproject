package com.project.yomozomo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sub_category")
@Getter @Setter
public class SubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_category_id")
    private int subCategoryId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

}
