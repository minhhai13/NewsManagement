package com.minhhai.newsmanagement.security;

import com.minhhai.newsmanagement.entity.UserAccount;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
    private CurrentUser() {}

    public static UserAccount get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserAccount user)) {
            throw new org.springframework.security.access.AccessDeniedException("Chưa đăng nhập");
        }
        return user;
    }
}
