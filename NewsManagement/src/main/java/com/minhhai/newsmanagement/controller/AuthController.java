package com.minhhai.newsmanagement.controller;

import com.minhhai.newsmanagement.dto.AuthResponse;
import com.minhhai.newsmanagement.dto.LoginRequest;
import com.minhhai.newsmanagement.dto.UserResponse;
import com.minhhai.newsmanagement.security.CurrentUser;
import com.minhhai.newsmanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(CurrentUser.get());
    }
}
