package com.project.yomozomo.dto;

import java.util.List;

public record ProductPageDto(
        List<ProductDto> products,
        int totalPages
) {}
