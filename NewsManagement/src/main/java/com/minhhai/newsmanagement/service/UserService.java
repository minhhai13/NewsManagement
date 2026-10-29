package com.minhhai.newsmanagement.service;

import com.minhhai.newsmanagement.dto.UserRequest;
import com.minhhai.newsmanagement.dto.UserResponse;
import com.minhhai.newsmanagement.entity.UserAccount;
import com.minhhai.newsmanagement.repository.NewsArticleRepository;
import com.minhhai.newsmanagement.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final NewsArticleRepository articleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, NewsArticleRepository articleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return UserResponse.from(find(id));
    }

    public UserResponse create(UserRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu là bắt buộc khi tạo tài khoản");
        }
        if (userRepository.existsByUsername(req.username().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username đã tồn tại");
        }
        UserAccount u = new UserAccount();
        u.setUsername(req.username().trim());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setName(req.name().trim());
        u.setRole(req.role());
        u.setStatus(req.status() == null ? 1 : req.status());
        return UserResponse.from(userRepository.save(u));
    }

    public UserResponse update(Long id, UserRequest req, UserAccount actor) {
        UserAccount u = find(id);
        String username = req.username().trim();
        if (!u.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username đã tồn tại");
        }
        if (u.getId().equals(actor.getId())
                && (req.role() != u.getRole() || Integer.valueOf(0).equals(req.status()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể tự đổi role hoặc tự khóa tài khoản của mình");
        }
        u.setUsername(username);
        u.setName(req.name().trim());
        u.setRole(req.role());
        if (req.status() != null) u.setStatus(req.status());
        if (req.password() != null && !req.password().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(req.password()));
        }
        return UserResponse.from(userRepository.save(u));
    }

    /** User đã có bài viết thì khóa (status = 0), chưa có gì thì xóa hẳn. */
    public String remove(Long id, UserAccount actor) {
        UserAccount u = find(id);
        if (u.getId().equals(actor.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể tự xóa tài khoản của mình");
        }
        if (articleRepository.countByAuthorId(id) > 0) {
            u.setStatus(0);
            userRepository.save(u);
            return "Tài khoản đã có bài viết nên được khóa thay vì xóa";
        }
        try {
            userRepository.delete(u);
            userRepository.flush();
        } catch (Exception e) { // đã có log workflow tham chiếu: khóa thay vì xóa
            u.setStatus(0);
            userRepository.save(u);
            return "Tài khoản đã có lịch sử thao tác nên được khóa thay vì xóa";
        }
        return "Đã xóa tài khoản";
    }

    private UserAccount find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
    }
}
