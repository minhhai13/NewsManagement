package com.minhhai.newsmanagement.dto;

import com.minhhai.newsmanagement.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
        @NotBlank(message = "Username không được để trống") String username,
        String password, // bắt buộc khi tạo mới, bỏ trống khi sửa để giữ mật khẩu cũ
        @NotBlank(message = "Họ và tên không được để trống") String name,
        @NotNull(message = "Role là bắt buộc") UserRole role,
        Integer status) {
}
