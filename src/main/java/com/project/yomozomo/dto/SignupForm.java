package com.project.yomozomo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupForm {
    @NotBlank
    @Pattern(regexp="^[A-Za-z][A-Za-z0-9]{3,29}$",
            message="영문으로 시작하고 영문·숫자 조합, 4~30자")
    private String username;
    private String nickname;
    private String email;
    private String name;
    @NotBlank
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,20}$",
            message = "8~20자, 영문·숫자·특수문자 포함"
    )
    private String password;
    private String gender;
    private LocalDate birthdate;
    private String phone;
    private String address;        // 도로명 주소
    private String addressDetail;  // 상세 주소
    private String zipNo;          // 우편번호
    private String referral;
    private String profileImageUrl;

}
