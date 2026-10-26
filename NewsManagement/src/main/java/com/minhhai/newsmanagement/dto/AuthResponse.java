package com.minhhai.newsmanagement.dto;

public record AuthResponse(String token, String tokenType, UserResponse user) {
}
