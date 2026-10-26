package com.minhhai.newsmanagement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank(message = "Tên danh mục không được để trống") String name,
        String description,
        @Min(value = 0, message = "Status phải là 0 hoặc 1") @Max(value = 1, message = "Status phải là 0 hoặc 1") Integer status,
        Long parentCategoryId) {
}
