package com.project.yomozomo.dto;

import lombok.Data;

@Data
public class ReportRequestDto {
    private Long reporterId;
    private Integer productId;
    private String title;
    private String details;
}
