package com.minhhai.newsmanagement.dto;

import com.minhhai.newsmanagement.entity.Category;

public record CategoryResponse(Long id, String name, String description, Integer status, Long parentCategoryId) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), c.getStatus(),
                c.getParentCategory() != null ? c.getParentCategory().getId() : null);
    }
}
