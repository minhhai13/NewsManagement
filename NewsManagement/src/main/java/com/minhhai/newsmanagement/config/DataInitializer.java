package com.minhhai.newsmanagement.config;

import com.minhhai.newsmanagement.entity.Category;
import com.minhhai.newsmanagement.entity.NewsArticle;
import com.minhhai.newsmanagement.entity.UserAccount;
import com.minhhai.newsmanagement.entity.WorkflowAuditLog;
import com.minhhai.newsmanagement.enums.ArticleStatus;
import com.minhhai.newsmanagement.enums.UserRole;
import com.minhhai.newsmanagement.enums.WorkflowAction;
import com.minhhai.newsmanagement.repository.CategoryRepository;
import com.minhhai.newsmanagement.repository.NewsArticleRepository;
import com.minhhai.newsmanagement.repository.UserRepository;
import com.minhhai.newsmanagement.repository.WorkflowAuditLogRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/** Dữ liệu mẫu khởi tạo cho demo khi DB còn trống hoặc thiếu bài viết. */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final NewsArticleRepository newsArticleRepository;
    private final WorkflowAuditLogRepository workflowAuditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           NewsArticleRepository newsArticleRepository,
                           WorkflowAuditLogRepository workflowAuditLogRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.newsArticleRepository = newsArticleRepository;
        this.workflowAuditLogRepository = workflowAuditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initUsersAndCategories();
        initArticlesAndWorkflowLogs();
    }

    private void initUsersAndCategories() {
        getOrCreateUser("admin", "admin123", "Quản trị viên", UserRole.ADMIN);
        getOrCreateUser("editor", "editor123", "Biên tập viên", UserRole.EDITOR);
        getOrCreateUser("contributor1", "123456", "Nguyễn Minh Anh", UserRole.CONTRIBUTOR);
        getOrCreateUser("contributor2", "123456", "Trần Văn An", UserRole.CONTRIBUTOR);
        getOrCreateUser("contributor3", "123456", "Lê Thị Thảo", UserRole.CONTRIBUTOR);

        // Tạo danh mục cha
        Category tb = getOrCreateCategory("Thông báo chung", "Thông báo chính thức, văn bản và quy định của nhà trường", null);
        Category ht = getOrCreateCategory("Học thuật & Đào tạo", "Kế hoạch giảng dạy, lịch thi, đăng ký môn học và học phần", null);
        Category sk = getOrCreateCategory("Sự kiện & Đoàn hội", "Hoạt động phong trào thanh niên, câu lạc bộ và các cuộc thi sinh viên", null);
        Category kh = getOrCreateCategory("Khoa học & Công nghệ", "Đề tài nghiên cứu, công nghệ mới, hội thảo khoa học và khởi nghiệp", null);

        // Tạo danh mục con
        getOrCreateCategory("Thông báo khẩn", "Cảnh báo thời tiết, nghỉ học đột xuất, sự cố hạ tầng kỹ thuật", tb);
        getOrCreateCategory("Tuyển sinh & Học bổng", "Thông tin xét tuyển đại học, sau đại học và các suất học bổng tài trợ", ht);
    }

    private void initArticlesAndWorkflowLogs() {
        if (newsArticleRepository.count() > 0) {
            return;
        }

        UserAccount editor = userRepository.findByUsername("editor").orElse(null);
        UserAccount c1 = userRepository.findByUsername("contributor1").orElse(null);
        UserAccount c2 = userRepository.findByUsername("contributor2").orElse(null);
        UserAccount c3 = userRepository.findByUsername("contributor3").orElse(c1);

        Category cDaoTao = findCategoryByName("Học thuật & Đào tạo");
        Category cThongBao = findCategoryByName("Thông báo chung");
        Category cSuKien = findCategoryByName("Sự kiện & Đoàn hội");
        Category cKhoaHoc = findCategoryByName("Khoa học & Công nghệ");
        Category cHocBong = findCategoryByName("Tuyển sinh & Học bổng");
        Category cKhan = findCategoryByName("Thông báo khẩn");

        Instant now = Instant.now();

        // 1. PUBLISHED - Bài đào tạo đăng ký học phần
        createArticle(
                "Kế hoạch đăng ký học phần và lịch thi kết thúc học phần HK2 (2025 - 2026)",
                "Phòng Quản lý Đào tạo thông báo thời gian mở cổng đăng ký môn học, hạn nộp học phí và quy định thi kết thúc học phần cho toàn thể sinh viên.",
                "### 1. Thời gian đăng ký học phần\n\n" +
                "- **Giai đoạn 1 (Đăng ký theo lộ trình chuẩn):** Từ 09h00 ngày 10/11/2025 đến 17h00 ngày 15/11/2025.\n" +
                "- **Giai đoạn 2 (Đăng ký học vượt, học cải thiện):** Từ 09h00 ngày 18/11/2025 đến 22h00 ngày 22/11/2025.\n" +
                "- **Giai đoạn 3 (Xử lý trùng lịch và hủy học phần):** Từ ngày 25/11/2025 đến hết ngày 28/11/2025.\n\n" +
                "### 2. Quy định và lưu ý đối với sinh viên\n\n" +
                "1. Sinh viên cần thanh toán học phí còn tồn đọng trước khi đăng ký hệ thống.\n" +
                "2. Kiểm tra kỹ tiên quyết của từng môn học trong khung chương trình đào tạo.\n" +
                "3. Mọi thắc mắc kỹ thuật vui lòng liên hệ phòng Đào tạo (Email: daotao@university.edu.vn).",
                "Đào tạo, Lịch thi, Học phần, Sinh viên",
                ArticleStatus.PUBLISHED,
                cDaoTao,
                c1,
                null,
                null,
                now.minus(Duration.ofDays(3)),
                now.minus(Duration.ofDays(4)),
                editor
        );

        // 2. PUBLISHED - Bài thể thao sự kiện
        createArticle(
                "Khai mạc Hội thao Sinh viên và Ngày hội Sức trẻ 2026",
                "Chuỗi hoạt động thể thao, hội trại và đêm hội âm nhạc bùng nổ thu hút hơn 3.000 sinh viên tham gia tranh tài ở 8 bộ môn.",
                "Sáng ngày 25/10, tại Khu phức hợp thể thao đa năng, Đoàn Thanh niên phối hợp cùng Hội Sinh viên trường đã long trọng tổ chức Lễ Khai mạc **Hội thao Sinh viên 2026**.\n\n" +
                "Năm nay, hội thao quy tụ hơn **60 đoàn vận động viên** đến từ tất cả các khoa viện với các nội dung thi đấu hấp dẫn như:\n" +
                "- Bóng đá nam/nữ sân 7\n" +
                "- Bóng rổ 3x3 và 5x5\n" +
                "- Cầu lông và Bóng bàn đơn/đôi\n" +
                "- Kéo co và Chạy tiếp sức phong trào\n\n" +
                "Đặc biệt, đêm Gala bế mạc và trao giải dự kiến sẽ diễn ra vào cuối tuần sau với sự góp mặt của các ca sĩ khách mời nổi tiếng.",
                "Hội thao, Thể thao, Đoàn thanh niên, Sự kiện",
                ArticleStatus.PUBLISHED,
                cSuKien,
                c2,
                null,
                null,
                now.minus(Duration.ofDays(1)),
                now.minus(Duration.ofDays(2)),
                editor
        );

        // 3. PUBLISHED - Bài nghiên cứu khoa học
        createArticle(
                "Nhóm nghiên cứu sinh viên giành giải Nhất Cuộc thi Sáng tạo AI Quốc tế",
                "Đề tài ứng dụng mô hình Deep Learning trong chẩn đoán sớm hình ảnh y khoa đã xuất sắc vượt qua 60 đại diện từ các trường đại học quốc tế.",
                "Đội thi **AI-Care Med** đại diện cho Trường đã xuất sắc vượt qua các vòng thẩm định gắt gao của hội đồng giám khảo quốc tế tại Tokyo để mang về giải thưởng cao nhất.\n\n" +
                "### Về đề tài nghiên cứu:\n" +
                "Hệ thống ứng dụng kiến trúc Transformer kết hợp xử lý thị giác máy tính, cho phép phân tích phim chụp X-quang phổi với độ chính xác đạt trên **98.4%**, giảm thời gian chẩn đoán từ 30 phút xuống còn dưới 10 giây.\n\n" +
                "Đề tài hiện đang được thử nghiệm tiền lâm sàng tại 2 bệnh viện tuyến trung ương và chuẩn bị công bố trên tạp chí IEEE Transactions.",
                "Nghiên cứu khoa học, Trí tuệ nhân tạo, AI, Công nghệ",
                ArticleStatus.PUBLISHED,
                cKhoaHoc,
                c3,
                null,
                null,
                now.minus(Duration.ofDays(5)),
                now.minus(Duration.ofDays(6)),
                editor
        );

        // 4. PUBLISHED - Bài học bổng doanh nghiệp
        createArticle(
                "Thông báo chương trình Học bổng Tài năng Công nghệ kỳ Mùa Xuân 2026",
                "Quỹ Hỗ trợ tài năng sinh viên công bố 100 suất học bổng toàn phần và bán phần từ các tập đoàn công nghệ với tổng giá trị hơn 1.5 tỷ đồng.",
                "Nhằm đồng hành và khích lệ các bạn sinh viên có thành tích học tập và rèn luyện xuất sắc, Nhà trường phối hợp cùng đối tác doanh nghiệp triển khai gói học bổng thường niên:\n\n" +
                "### Điều kiện ứng tuyển:\n" +
                "- Sinh viên năm 2 trở lên thuộc các khối ngành Kỹ thuật, Công nghệ thông tin và Kinh tế số.\n" +
                "- Điểm trung bình tích lũy GPA từ **3.2/4.0** trở lên.\n" +
                "- Điểm rèn luyện học kỳ gần nhất đạt loại Tốt trở lên (>= 80 điểm).\n" +
                "- Ưu tiên sinh viên có chứng chỉ tiếng Anh quốc tế hoặc giải thưởng học thuật.\n\n" +
                "Hạn nộp hồ sơ trực tuyến: Trước 17h00 ngày 20 hàng tháng qua cổng dịch vụ sinh viên.",
                "Học bổng, Doanh nghiệp, Tuyển sinh, Hỗ trợ sinh viên",
                ArticleStatus.PUBLISHED,
                cHocBong != null ? cHocBong : cDaoTao,
                c1,
                null,
                null,
                now.minus(Duration.ofDays(7)),
                now.minus(Duration.ofDays(8)),
                editor
        );

        // 5. PENDING_REVIEW - Hướng dẫn đánh giá điểm rèn luyện
        createArticle(
                "Hướng dẫn sinh viên thực hiện đánh giá kết quả rèn luyện trực tuyến",
                "Phòng Công tác Sinh viên ban hành quy trình tự chấm điểm và nộp minh chứng hoạt động phong trào trên cổng thông tin điện tử mới.",
                "Nhằm chuyển đổi số toàn diện các thủ tục hành chính sinh viên, từ học kỳ này việc đánh giá điểm rèn luyện sẽ được thực hiện 100% online.\n\n" +
                "### Các bước thực hiện:\n" +
                "1. **Bước 1:** Đăng nhập hệ thống bằng email trường được cấp.\n" +
                "2. **Bước 2:** Vào mục *Rèn luyện* -> Chọn học kỳ cần đánh giá.\n" +
                "3. **Bước 3:** Tự chấm điểm theo các tiêu chí và đính kèm file ảnh/PDF minh chứng.\n" +
                "4. **Bước 4:** Bấm *Gửi duyệt* để Cán sự lớp và Cố vấn học tập chấm điểm.\n\n" +
                "Thời hạn sinh viên hoàn thành tự đánh giá: Đến hết ngày 30/11.",
                "Điểm rèn luyện, Hướng dẫn, Công tác sinh viên",
                ArticleStatus.PENDING_REVIEW,
                cThongBao,
                c1,
                null,
                null,
                null,
                now.minus(Duration.ofHours(3)),
                null
        );

        // 6. PENDING_REVIEW - Thành lập Innovation Hub
        createArticle(
                "Thành lập Không gian Đổi mới Sáng tạo & Ươm mầm Khởi nghiệp Sinh viên",
                "Mô hình Không gian sáng tạo mới sẽ mở cửa 24/7 cung cấp thiết bị in 3D, phòng lab IoT và gói tài trợ hạt giống cho các dự án tiềm năng.",
                "Nhằm tạo môi trường thực hành tối đa cho sinh viên đam mê công nghệ và kinh doanh, Trung tâm Đổi mới Sáng tạo trân trọng giới thiệu **Innovation Hub 2026**.\n\n" +
                "Không gian được trang bị:\n" +
                "- 20 trạm máy tính cấu hình cao phục vụ huấn luyện mô hình AI\n" +
                "- Xưởng chế tác nhanh với 5 máy in 3D công nghiệp và máy cắt laser\n" +
                "- Phòng họp cách âm phục vụ thuyết trình gọi vốn và pitching dự án\n\n" +
                "Các nhóm sinh viên có dự án có thể đăng ký sử dụng miễn phí ngay từ tuần tới.",
                "Khởi nghiệp, Innovation Hub, Công nghệ, Sinh viên",
                ArticleStatus.PENDING_REVIEW,
                cKhoaHoc,
                c2,
                null,
                null,
                null,
                now.minus(Duration.ofHours(6)),
                null
        );

        // 7. CHANGES_REQUESTED - Lễ tốt nghiệp (Editor yêu cầu sửa)
        createArticle(
                "Kế hoạch tổ chức Lễ trao bằng tốt nghiệp đợt 1 năm học 2026",
                "Dự thảo thông báo về thời gian, địa điểm và quy trình tổ chức trao bằng cho sinh viên các khoa hoàn thành chương trình sớm.",
                "Nhà trường trân trọng thông báo kế hoạch tổ chức Lễ trao bằng tốt nghiệp đợt 1 năm 2026:\n\n" +
                "- **Thời gian:** 08h00 Thứ Bảy, ngày 15/12/2026.\n" +
                "- **Địa điểm:** Hội trường Lớn (Nhà A).\n" +
                "- **Thành phần:** Toàn thể sinh viên tốt nghiệp được công nhận và phụ huynh tham dự.\n\n" +
                "Chi tiết đăng ký lễ phục và nhận vé mời phụ huynh sẽ được thông báo cụ thể sau.",
                "Lễ tốt nghiệp, Tân cử nhân, Thông báo",
                ArticleStatus.CHANGES_REQUESTED,
                cThongBao,
                c1,
                "Vui lòng bổ sung chi tiết: 1) Sơ đồ phân luồng bãi đỗ xe và cửa vào sảnh A cho phụ huynh; 2) Lịch mượn trả lễ phục tốt nghiệp theo từng khung giờ của các khoa; 3) Số hotline hỗ trợ giải đáp thắc mắc.",
                null,
                null,
                now.minus(Duration.ofDays(2)),
                editor
        );

        // 8. APPROVED_SCHEDULED - Thông báo nghỉ lễ 30/4 - 1/5 (Hẹn giờ)
        createArticle(
                "Thông báo lịch nghỉ lễ Giỗ Tổ Hùng Vương và kỳ nghỉ 30/4 - 1/5",
                "Kế hoạch nghỉ lễ chính thức và lịch học bù trực tuyến cho cán bộ, giảng viên và sinh viên toàn trường.",
                "Căn cứ Bộ luật Lao động và thông báo của Bộ Lao động - Thương binh và Xã hội, Ban Giám hiệu thông báo lịch nghỉ lễ như sau:\n\n" +
                "1. **Nghỉ lễ Giỗ Tổ Hùng Vương:** Nghỉ 01 ngày (Thứ Ba, ngày 10/3 Âm lịch).\n" +
                "2. **Kỳ nghỉ 30/4 và 1/5:** Nghỉ liên tục 05 ngày theo lịch hoán đổi ngày làm việc.\n" +
                "3. **Kế hoạch học tập bù:** Giảng viên chủ động tổ chức học bù trực tuyến qua hệ thống E-learning trong thời gian trước kỳ thi cuối khóa.\n\n" +
                "Các đơn vị phòng ban bố trí cán bộ trực đảm bảo an ninh trật tự và an toàn phòng chống cháy nổ.",
                "Nghỉ lễ, Kế hoạch đào tạo, Thông báo",
                ArticleStatus.APPROVED_SCHEDULED,
                cThongBao,
                c2,
                null,
                now.plus(Duration.ofDays(2)),
                null,
                now.minus(Duration.ofHours(12)),
                editor
        );

        // 9. DRAFT - Bản nháp của contributor1
        createArticle(
                "[Nháp] Phóng sự ảnh: Một ngày trải nghiệm văn hóa của đoàn sinh viên quốc tế",
                "Ghi lại những khoảnh khắc đáng nhớ và ấn tượng sâu sắc của đoàn sinh viên trao đổi văn hóa đến từ Đại học Quốc gia Singapore.",
                "Đang hoàn thiện nội dung phỏng vấn và chọn lọc tư liệu ảnh độ phân giải cao...\n\n" +
                "- Phỏng vấn bạn Kevin Lee về cảm nhận ẩm thực đường phố Hà Nội\n" +
                "- Tham quan Trung tâm Bảo tồn Di sản văn hóa\n" +
                "- Giao lưu nghệ thuật dân gian cùng sinh viên CLB Âm nhạc truyền thống.",
                "Giao lưu quốc tế, Phóng sự, Sinh viên",
                ArticleStatus.DRAFT,
                cSuKien,
                c1,
                null,
                null,
                null,
                null,
                null
        );

        // 10. ARCHIVED - Thông báo bảo trì máy chủ cũ đã gỡ bài
        createArticle(
                "Thông báo tạm dừng cổng Đăng ký môn học để bảo trì hệ thống máy chủ định kỳ",
                "Hệ thống máy chủ dịch vụ đào tạo tạm ngưng hoạt động trong 6 giờ để nâng cấp vi chương trình và sao lưu cơ sở dữ liệu.",
                "Phòng Dịch vụ Công nghệ thông tin xin thông báo đợt bảo trì định kỳ đã hoàn tất đúng kế hoạch.\n\n" +
                "Hệ thống cơ sở dữ liệu đã được sao lưu toàn vẹn và nâng cấp băng thông chịu tải tăng thêm 40%, đáp ứng tốt nhu cầu đăng ký học phần trong đợt cao điểm sắp tới.",
                "Bảo trì, Kỹ thuật, Lưu trữ",
                ArticleStatus.ARCHIVED,
                cKhan != null ? cKhan : cThongBao,
                c2,
                null,
                null,
                now.minus(Duration.ofDays(30)),
                now.minus(Duration.ofDays(32)),
                editor
        );

        System.out.println("[NewsManagement] Đã khởi tạo thành công 10 bài viết mẫu cùng toàn bộ lịch sử Workflow Audit Log.");
    }

    private void createArticle(String title,
                               String headline,
                               String content,
                               String tags,
                               ArticleStatus status,
                               Category category,
                               UserAccount author,
                               String editorFeedback,
                               Instant scheduledPublishDate,
                               Instant publishedAt,
                               Instant submittedAt,
                               UserAccount editor) {
        NewsArticle a = new NewsArticle();
        a.setTitle(title);
        a.setHeadline(headline);
        a.setContent(content);
        a.setTags(tags);
        a.setStatus(status);
        a.setCategory(category);
        a.setAuthor(author);
        a.setEditorFeedback(editorFeedback);
        a.setScheduledPublishDate(scheduledPublishDate);
        a.setPublishedAt(publishedAt);
        a.setSubmittedAt(submittedAt);

        NewsArticle saved = newsArticleRepository.save(a);

        // Khởi tạo các dòng WorkflowAuditLog tương ứng với vòng đời bài viết
        workflowAuditLogRepository.save(new WorkflowAuditLog(saved, author, null, ArticleStatus.DRAFT, WorkflowAction.CREATE_DRAFT, "Tác giả tạo bản nháp"));

        if (status == ArticleStatus.PENDING_REVIEW) {
            workflowAuditLogRepository.save(new WorkflowAuditLog(saved, author, ArticleStatus.DRAFT, ArticleStatus.PENDING_REVIEW, WorkflowAction.SUBMIT, "Gửi bài chờ biên tập viên duyệt"));
        } else if (status == ArticleStatus.CHANGES_REQUESTED) {
            workflowAuditLogRepository.save(new WorkflowAuditLog(saved, author, ArticleStatus.DRAFT, ArticleStatus.PENDING_REVIEW, WorkflowAction.SUBMIT, "Gửi bài chờ biên tập viên duyệt"));
            workflowAuditLogRepository.save(new WorkflowAuditLog(saved, editor, ArticleStatus.PENDING_REVIEW, ArticleStatus.CHANGES_REQUESTED, WorkflowAction.REQUEST_CHANGES, editorFeedback));
        } else if (status == ArticleStatus.APPROVED_SCHEDULED) {
            workflowAuditLogRepository.save(new WorkflowAuditLog(saved, author, ArticleStatus.DRAFT, ArticleStatus.PENDING_REVIEW, WorkflowAction.SUBMIT, "Gửi bài chờ biên tập viên duyệt"));
            workflowAuditLogRepository.save(new WorkflowAuditLog(saved, editor, ArticleStatus.PENDING_REVIEW, ArticleStatus.APPROVED_SCHEDULED, WorkflowAction.SCHEDULE, "Đã duyệt và hẹn giờ xuất bản"));
        } else if (status == ArticleStatus.PUBLISHED) {
            workflowAuditLogRepository.save(new WorkflowAuditLog(saved, author, ArticleStatus.DRAFT, ArticleStatus.PENDING_REVIEW, WorkflowAction.SUBMIT, "Gửi bài chờ biên tập viên duyệt"));
            workflowAuditLogRepository.save(new WorkflowAuditLog(saved, editor, ArticleStatus.PENDING_REVIEW, ArticleStatus.PUBLISHED, WorkflowAction.APPROVE_NOW, "Bài viết đạt tiêu chuẩn và đã được xuất bản"));
        } else if (status == ArticleStatus.ARCHIVED) {
            workflowAuditLogRepository.save(new WorkflowAuditLog(saved, author, ArticleStatus.DRAFT, ArticleStatus.PENDING_REVIEW, WorkflowAction.SUBMIT, "Gửi bài chờ biên tập viên duyệt"));
            workflowAuditLogRepository.save(new WorkflowAuditLog(saved, editor, ArticleStatus.PENDING_REVIEW, ArticleStatus.PUBLISHED, WorkflowAction.APPROVE_NOW, "Đã xuất bản trước đó"));
            workflowAuditLogRepository.save(new WorkflowAuditLog(saved, editor, ArticleStatus.PUBLISHED, ArticleStatus.ARCHIVED, WorkflowAction.ARCHIVE, "Chuyển bài viết vào kho lưu trữ do nội dung đã hết hiệu lực"));
        }
    }

    private UserAccount getOrCreateUser(String username, String password, String name, UserRole role) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            UserAccount u = new UserAccount();
            u.setUsername(username);
            u.setPasswordHash(passwordEncoder.encode(password));
            u.setName(name);
            u.setRole(role);
            return userRepository.save(u);
        });
    }

    private Category getOrCreateCategory(String name, String description, Category parent) {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName(name);
                    c.setDescription(description);
                    c.setStatus(1);
                    c.setParentCategory(parent);
                    return categoryRepository.save(c);
                });
    }

    private Category findCategoryByName(String name) {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
