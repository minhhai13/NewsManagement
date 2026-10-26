package com.minhhai.newsmanagement.service;

import com.minhhai.newsmanagement.entity.NewsArticle;
import com.minhhai.newsmanagement.entity.UserAccount;
import com.minhhai.newsmanagement.entity.WorkflowAuditLog;
import com.minhhai.newsmanagement.enums.ArticleStatus;
import com.minhhai.newsmanagement.enums.UserRole;
import com.minhhai.newsmanagement.enums.WorkflowAction;
import com.minhhai.newsmanagement.repository.NewsArticleRepository;
import com.minhhai.newsmanagement.repository.UserRepository;
import com.minhhai.newsmanagement.repository.WorkflowAuditLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static com.minhhai.newsmanagement.enums.ArticleStatus.*;

/**
 * Nơi duy nhất đổi trạng thái bài viết. Mỗi chuyển trạng thái thành công ghi đúng một audit log
 * trong cùng transaction.
 */
@Service
public class WorkflowService {

    private final NewsArticleRepository articleRepository;
    private final WorkflowAuditLogRepository auditRepository;
    private final UserRepository userRepository;

    public WorkflowService(NewsArticleRepository articleRepository, WorkflowAuditLogRepository auditRepository,
                           UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
    }

    // ---------- ghi log ----------

    /** Dùng cho CREATE_DRAFT (không có from). */
    @Transactional
    public void logCreate(NewsArticle article, UserAccount actor) {
        auditRepository.save(new WorkflowAuditLog(article, actor, null, DRAFT, WorkflowAction.CREATE_DRAFT, null));
    }

    // ---------- Contributor ----------

    @Transactional
    public NewsArticle submit(Long id, UserAccount actor) {
        return sendForReview(id, actor, DRAFT, WorkflowAction.SUBMIT);
    }

    @Transactional
    public NewsArticle resubmit(Long id, UserAccount actor) {
        return sendForReview(id, actor, CHANGES_REQUESTED, WorkflowAction.RESUBMIT);
    }

    private NewsArticle sendForReview(Long id, UserAccount actor, ArticleStatus expected, WorkflowAction action) {
        NewsArticle a = load(id);
        if (actor.getRole() != UserRole.CONTRIBUTOR || !a.getAuthor().getId().equals(actor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ tác giả của bài mới được gửi duyệt");
        }
        requireStatus(a, expected, action);
        requireComplete(a);
        a.setSubmittedAt(Instant.now());
        return move(a, PENDING_REVIEW, action, actor, null);
    }

    // ---------- Editor ----------

    @Transactional
    public NewsArticle requestChanges(Long id, UserAccount actor, String feedback) {
        requireRole(actor, UserRole.EDITOR);
        if (feedback == null || feedback.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Feedback là bắt buộc khi yêu cầu sửa");
        }
        NewsArticle a = load(id);
        requireStatus(a, PENDING_REVIEW, WorkflowAction.REQUEST_CHANGES);
        a.setEditorFeedback(feedback.trim());
        return move(a, CHANGES_REQUESTED, WorkflowAction.REQUEST_CHANGES, actor, feedback.trim());
    }

    /** scheduledAt == null: đăng ngay; ngược lại hẹn giờ (phải ở tương lai). */
    @Transactional
    public NewsArticle approve(Long id, UserAccount actor, Instant scheduledAt) {
        requireRole(actor, UserRole.EDITOR);
        NewsArticle a = load(id);
        requireStatus(a, PENDING_REVIEW, scheduledAt == null ? WorkflowAction.APPROVE_NOW : WorkflowAction.SCHEDULE);
        if (scheduledAt == null) {
            a.setScheduledPublishDate(null);
            a.setPublishedAt(Instant.now());
            return move(a, PUBLISHED, WorkflowAction.APPROVE_NOW, actor, null);
        }
        if (!scheduledAt.isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giờ hẹn phải ở tương lai");
        }
        a.setScheduledPublishDate(scheduledAt);
        return move(a, APPROVED_SCHEDULED, WorkflowAction.SCHEDULE, actor, null);
    }

    @Transactional
    public NewsArticle archive(Long id, UserAccount actor, String reason) {
        if (actor.getRole() != UserRole.EDITOR && actor.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ Editor hoặc Admin được gỡ bài");
        }
        NewsArticle a = load(id);
        if (a.getStatus() != PUBLISHED && a.getStatus() != APPROVED_SCHEDULED) {
            throw invalid(a, WorkflowAction.ARCHIVE);
        }
        a.setScheduledPublishDate(null);
        return move(a, ARCHIVED, WorkflowAction.ARCHIVE, actor, blankToNull(reason));
    }

    // ---------- Scheduler ----------

    /** Mỗi bài một transaction; trả về false nếu bài không còn đủ điều kiện (ví dụ vừa bị Archive). */
    @Transactional
    public boolean autoPublish(Long id) {
        NewsArticle a = articleRepository.findById(id).orElse(null);
        if (a == null || a.getStatus() != APPROVED_SCHEDULED || a.getScheduledPublishDate() == null
                || a.getScheduledPublishDate().isAfter(Instant.now())) {
            return false;
        }
        a.setPublishedAt(Instant.now());
        move(a, PUBLISHED, WorkflowAction.AUTO_PUBLISH, null, null);
        return true;
    }

    // ---------- Admin override ----------

    @Transactional
    public NewsArticle override(Long id, UserAccount actor, ArticleStatus target, String reason, Instant scheduledAt) {
        requireRole(actor, UserRole.ADMIN);
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lý do override là bắt buộc");
        }
        NewsArticle a = load(id);
        if (a.getStatus() == target) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bài đã ở trạng thái " + target);
        }
        switch (target) {
            case APPROVED_SCHEDULED -> {
                if (scheduledAt == null || !scheduledAt.isAfter(Instant.now())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chuyển sang hẹn giờ cần giờ hẹn ở tương lai");
                }
                requireComplete(a);
                a.setScheduledPublishDate(scheduledAt);
            }
            case PUBLISHED -> {
                requireComplete(a);
                a.setScheduledPublishDate(null);
                a.setPublishedAt(Instant.now());
            }
            case PENDING_REVIEW -> {
                requireComplete(a);
                a.setSubmittedAt(Instant.now());
                a.setScheduledPublishDate(null);
            }
            case CHANGES_REQUESTED -> {
                a.setEditorFeedback(reason.trim());
                a.setScheduledPublishDate(null);
            }
            default -> a.setScheduledPublishDate(null); // DRAFT, ARCHIVED
        }
        return move(a, target, WorkflowAction.OVERRIDE, actor, reason.trim());
    }

    // ---------- helper ----------

    /** Đổi trạng thái và ghi log. Gọi sau khi đã kiểm tra quyền và điều kiện. */
    private NewsArticle move(NewsArticle a, ArticleStatus to, WorkflowAction action, UserAccount actor, String comment) {
        ArticleStatus from = a.getStatus();
        a.setStatus(to);
        NewsArticle saved = articleRepository.saveAndFlush(a); // flush để @Version xung đột lộ ra ngay
        UserAccount actorRef = actor == null ? null : userRepository.getReferenceById(actor.getId());
        auditRepository.save(new WorkflowAuditLog(saved, actorRef, from, to, action, comment));
        return saved;
    }

    private NewsArticle load(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết"));
    }

    private void requireRole(UserAccount actor, UserRole role) {
        if (actor.getRole() != role) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ " + role + " được thực hiện thao tác này");
        }
    }

    private void requireStatus(NewsArticle a, ArticleStatus expected, WorkflowAction action) {
        if (a.getStatus() != expected) throw invalid(a, action);
    }

    private ResponseStatusException invalid(NewsArticle a, WorkflowAction action) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "Không thể " + action + " khi bài đang ở trạng thái " + a.getStatus());
    }

    private void requireComplete(NewsArticle a) {
        if (isBlank(a.getTitle()) || isBlank(a.getContent()) || a.getCategory() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bài cần đủ tiêu đề, nội dung và danh mục");
        }
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static String blankToNull(String s) { return isBlank(s) ? null : s.trim(); }
}
