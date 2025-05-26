package com.project.yomozomo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "USERS")
@Getter @Setter @NoArgsConstructor

public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq_gen")
    @SequenceGenerator(name = "users_seq_gen", sequenceName = "USERS_SEQ", allocationSize = 1)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "username", length = 30, nullable = false, unique = true)
    private String username;

    @Column(name = "nickname", length = 50, unique = true)
    private String nickname;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "gender", length = 1, nullable = false)
    private String gender; // 'M' or 'F'

    @Column(name = "birth_date")
    private LocalDate birthdate;

    @Column(name = "referral_code", length = 20)
    private String referral;

    @Column(name = "phone_number", length = 20)
    private String phone;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "zip_no")
    private String zipNo;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "join_date")
    private LocalDateTime signupDate = LocalDateTime.now();

    @Column(name = "profile_image", length = 300)
    private String profileImageUrl;

    @Column(name = "is_business", length = 1)
    private String isBusiness;

    @Column(name = "business_number", length = 30)
    private String businessNumber;

    @Column(name = "user_grade", length = 20)
    private String grade;

    @Column(name = "user_rating", precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "is_dormant", length = 1)
    private String isDormant;

    @Column(name = "dormant_date")
    private LocalDate dormantDate;
}
