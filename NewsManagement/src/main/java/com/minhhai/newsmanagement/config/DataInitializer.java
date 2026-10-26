package com.minhhai.newsmanagement.config;

import com.minhhai.newsmanagement.entity.Category;
import com.minhhai.newsmanagement.entity.UserAccount;
import com.minhhai.newsmanagement.enums.UserRole;
import com.minhhai.newsmanagement.repository.CategoryRepository;
import com.minhhai.newsmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Dữ liệu mẫu cho demo khi DB còn trống. */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, CategoryRepository categoryRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            createUser("admin", "admin123", "Quản trị viên", UserRole.ADMIN);
            createUser("editor", "editor123", "Biên tập viên", UserRole.EDITOR);
            createUser("contributor1", "123456", "Nguyễn Minh Anh", UserRole.CONTRIBUTOR);
            createUser("contributor2", "123456", "Trần Văn A", UserRole.CONTRIBUTOR);
            System.out.println("[NewsManagement] Tài khoản mẫu: admin/admin123, editor/editor123, contributor1/123456, contributor2/123456");
        }
        if (categoryRepository.count() == 0) {
            createCategory("Thông báo", "Thông báo chung của trường");
            createCategory("Học thuật", "Lịch thi, đăng ký, tuyển sinh");
            createCategory("Sự kiện", "Hoạt động và sự kiện sinh viên");
        }
    }

    private void createUser(String username, String password, String name, UserRole role) {
        UserAccount u = new UserAccount();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setName(name);
        u.setRole(role);
        userRepository.save(u);
    }

    private void createCategory(String name, String description) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(description);
        categoryRepository.save(c);
    }
}
