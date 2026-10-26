package com.minhhai.newsmanagement.service;

import com.minhhai.newsmanagement.dto.AuthResponse;
import com.minhhai.newsmanagement.dto.LoginRequest;
import com.minhhai.newsmanagement.dto.UserResponse;
import com.minhhai.newsmanagement.entity.UserAccount;
import com.minhhai.newsmanagement.repository.UserRepository;
import com.minhhai.newsmanagement.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản hoặc mật khẩu không chính xác"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản hoặc mật khẩu không chính xác");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản đã bị vô hiệu hóa");
        }
        return new AuthResponse(jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name()),
                "Bearer", UserResponse.from(user));
    }
}
