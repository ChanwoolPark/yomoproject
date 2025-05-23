package com.project.yomozomo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RentalRequestDto {
    private int productId;
    private String startDate;
    private String endDate;
}