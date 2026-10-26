package com.minhhai.newsmanagement.dto;

import com.minhhai.newsmanagement.entity.UserAccount;
import com.minhhai.newsmanagement.enums.UserRole;

public record UserResponse(Long id, String username, String name, UserRole role, Integer status) {
    public static UserResponse from(UserAccount u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getName(), u.getRole(), u.getStatus());
    }
}
