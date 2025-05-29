package com.project.yomozomo.dto;

import lombok.Data;

@Data
public class UserEditForm {
    private String nickname;
    private String email;
    private String phone;
    private String address;
    private String addressDetail;
    private String zipNo;
}
