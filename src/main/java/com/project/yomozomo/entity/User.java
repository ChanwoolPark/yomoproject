package com.project.yomozomo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;//패스워드는 민감한 자료여서
@Entity
@Table(name = "USERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @JsonIgnore
    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "gender", length = 1, nullable = false)
    private String gender;                // 'M' or 'F'

    @Column(name = "birth_date")
    private LocalDate birthdate;

    @Column(name = "referral_code", length = 20)
    private String referral;

    @Column(name = "phone_number", length = 20)
    private String phone;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name="zipNo")
    private String zipNo;

    @Column(name="address_detail")
    private String addressDetail;

    @Column(name = "join_date")
    private LocalDateTime signupDate = LocalDateTime.now();

    @Column(name = "profile_image", length = 300)
    private String profileImageUrl;

    @Column(name = "is_business", length = 1)
    private String isBusiness;            // 'Y' or 'N'

    @Column(name = "business_number", length = 30)
    private String businessNumber;

    @Column(name = "user_grade", length = 20)
    private String grade;

    // NUMBER(2,1) 은 BigDecimal + precision/scale 로 매핑합니다
    @Column(name = "user_rating", precision = 3, scale = 1)
    private BigDecimal rating = BigDecimal.valueOf(0.0);

    @Column(name = "review_count")
    private Integer reviewCount;

    // 휴면 계정 관련
    @Column(name = "is_dormant", length = 1)
    private String isDormant = "N"; // 'Y' or 'N'

    @Column(name = "dormant_date")
    private LocalDate dormantDate;

    // User.java
    @Column(name = "last_login_date")
    private LocalDate lastLoginDate; // or LocalDateTime


    // === 탈퇴/삭제 관련: 추가 컬럼 추천 ===
    // 1. 탈퇴 상태 표시 (soft delete)
    @Column(name = "is_withdrawn", length = 1)
    private String isWithdrawn = "N"; // 'Y' or 'N'

    // 2. 탈퇴 요청일 (삭제 예정일 계산용)
    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    // 관리자 계정
    @Column(nullable = false)
    private String role;

    @Column(name = "coupon_count")
    private Integer couponCount = 0;

    public String getRole() {
        return role; // 예시: "ADMIN" 또는 "USER"
    }

    @Column(name = "highest_balance")
    private int highestBalance;
}
