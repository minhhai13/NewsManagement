package com.minhhai.newsmanagement.service;

import com.minhhai.newsmanagement.dto.ArticleRequest;
import com.minhhai.newsmanagement.dto.ArticleResponse;
import com.minhhai.newsmanagement.dto.AuditLogResponse;
import com.minhhai.newsmanagement.entity.Category;
import com.minhhai.newsmanagement.entity.NewsArticle;
import com.minhhai.newsmanagement.entity.UserAccount;
import com.minhhai.newsmanagement.enums.ArticleStatus;
import com.minhhai.newsmanagement.enums.UserRole;
import com.minhhai.newsmanagement.repository.CategoryRepository;
import com.minhhai.newsmanagement.repository.NewsArticleRepository;
import com.minhhai.newsmanagement.repository.UserRepository;
import com.minhhai.newsmanagement.repository.WorkflowAuditLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.minhhai.newsmanagement.enums.ArticleStatus.*;

@Service
@Transactional(readOnly = true)
public class NewsService {

    private final NewsArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final WorkflowAuditLogRepository auditRepository;
    private final WorkflowService workflowService;

    public NewsService(NewsArticleRepository articleRepository, CategoryRepository categoryRepository,
                       UserRepository userRepository, WorkflowAuditLogRepository auditRepository,
                       WorkflowService workflowService) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.auditRepository = auditRepository;
        this.workflowService = workflowService;
    }

    // ---------- Contributor ----------

    @Transactional
    public ArticleResponse createDraft(ArticleRequest req, UserAccount user) {
        NewsArticle a = new NewsArticle();
        apply(a, req, null);
        a.setStatus(DRAFT);
        a.setAuthor(userRepository.getReferenceById(user.getId()));
        NewsArticle saved = articleRepository.save(a);
        workflowService.logCreate(saved, user);
        return ArticleResponse.from(saved);
    }

    @Transactional
    public ArticleResponse update(Long id, ArticleRequest req, UserAccount user) {
        NewsArticle a = find(id);
        requireAuthor(a, user);
        if (a.getStatus() != DRAFT && a.getStatus() != CHANGES_REQUESTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Chỉ sửa được bài ở trạng thái DRAFT hoặc CHANGES_REQUESTED (hiện tại: " + a.getStatus() + ")");
        }
        if (req.version() != null && !req.version().equals(a.getVersion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Bài viết vừa được thay đổi, vui lòng tải lại bài rồi thử lại");
        }
        apply(a, req, a.getCategory());
        return ArticleResponse.from(articleRepository.saveAndFlush(a));
    }

    public List<ArticleResponse> myArticles(UserAccount user) {
        return articleRepository.findByAuthorIdOrderByUpdatedAtDesc(user.getId())
                .stream().map(ArticleResponse::from).toList();
    }

    public ArticleResponse myArticle(Long id, UserAccount user) {
        NewsArticle a = find(id);
        requireAuthor(a, user);
        return ArticleResponse.from(a);
    }

    public long changesCount(UserAccount user) {
        return articleRepository.countByAuthorIdAndStatus(user.getId(), CHANGES_REQUESTED);
    }

    // ---------- Public ----------

    public List<ArticleResponse> publicList(Long categoryId) {
        List<NewsArticle> list = categoryId == null
                ? articleRepository.findByStatusOrderByPublishedAtDesc(PUBLISHED)
                : articleRepository.findByStatusAndCategoryIdOrderByPublishedAtDesc(PUBLISHED, categoryId);
        return list.stream().map(ArticleResponse::from).map(NewsService::publicView).toList();
    }

    public ArticleResponse publicDetail(Long id) {
        NewsArticle a = find(id);
        if (a.getStatus() != PUBLISHED) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết");
        return publicView(ArticleResponse.from(a));
    }

    /** Reader không cần thấy feedback nội bộ, giờ hẹn hay phiên bản. */
    private static ArticleResponse publicView(ArticleResponse r) {
        return new ArticleResponse(r.id(), r.title(), r.headline(), r.content(), r.tags(), r.status(),
                r.categoryId(), r.categoryName(), r.authorId(), r.authorName(),
                null, null, r.publishedAt(), null, r.createdAt(), null, null);
    }

    // ---------- Editor / Admin ----------

    public List<ArticleResponse> queue() {
        return articleRepository.findByStatusOrderBySubmittedAtAsc(PENDING_REVIEW)
                .stream().map(ArticleResponse::from).toList();
    }

    public List<ArticleResponse> listByStatus(ArticleStatus status, UserAccount user) {
        if (status == DRAFT && user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không được xem bản nháp của người khác");
        }
        return articleRepository.findByStatusOrderByUpdatedAtDesc(status)
                .stream().map(ArticleResponse::from).toList();
    }

    public ArticleResponse editorDetail(Long id, UserAccount user) {
        NewsArticle a = find(id);
        requireReadable(a, user);
        return ArticleResponse.from(a);
    }

    public List<AuditLogResponse> auditLogs(Long id, UserAccount user) {
        NewsArticle a = find(id);
        boolean isAuthor = a.getAuthor().getId().equals(user.getId());
        if (!isAuthor) requireReadable(a, user);
        return auditRepository.findByArticleIdOrderByCreatedAtAscIdAsc(id)
                .stream().map(AuditLogResponse::from).toList();
    }

    // ---------- helper ----------

    private void requireReadable(NewsArticle a, UserAccount user) {
        boolean ok = user.getRole() == UserRole.ADMIN
                || (user.getRole() == UserRole.EDITOR && a.getStatus() != DRAFT);
        if (!ok) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xem bài này");
    }

    private void requireAuthor(NewsArticle a, UserAccount user) {
        if (!a.getAuthor().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Đây không phải bài của bạn");
        }
    }

    private NewsArticle find(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết"));
    }

    /** Chỉ copy các trường nội dung; status/author không bao giờ đến từ form. */
    private void apply(NewsArticle a, ArticleRequest req, Category current) {
        a.setTitle(req.title().trim());
        a.setHeadline(req.headline());
        a.setContent(req.content());
        a.setTags(req.tags());
        if (req.categoryId() == null) {
            a.setCategory(null);
        } else if (current == null || !current.getId().equals(req.categoryId())) {
            Category c = categoryRepository.findById(req.categoryId()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh mục không tồn tại"));
            if (!Integer.valueOf(1).equals(c.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh mục đã bị vô hiệu hóa");
            }
            a.setCategory(c);
        }
    }
}
