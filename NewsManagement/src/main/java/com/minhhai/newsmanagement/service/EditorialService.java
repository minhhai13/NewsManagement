package com.minhhai.newsmanagement.service;

import com.minhhai.newsmanagement.dto.ArticleResponse;
import com.minhhai.newsmanagement.dto.AuditLogResponse;
import com.minhhai.newsmanagement.entity.NewsArticle;
import com.minhhai.newsmanagement.entity.UserAccount;
import com.minhhai.newsmanagement.enums.ArticleStatus;
import com.minhhai.newsmanagement.enums.UserRole;
import com.minhhai.newsmanagement.repository.NewsArticleRepository;
import com.minhhai.newsmanagement.repository.WorkflowAuditLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.minhhai.newsmanagement.enums.ArticleStatus.*;

/** Đọc bài cho Editor/Admin và lịch sử workflow. Mọi đổi trạng thái nằm ở WorkflowService. */
@Service
@Transactional(readOnly = true)
public class EditorialService {

    private final NewsArticleRepository articleRepository;
    private final WorkflowAuditLogRepository auditRepository;

    public EditorialService(NewsArticleRepository articleRepository, WorkflowAuditLogRepository auditRepository) {
        this.articleRepository = articleRepository;
        this.auditRepository = auditRepository;
    }

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

    private void requireReadable(NewsArticle a, UserAccount user) {
        boolean ok = user.getRole() == UserRole.ADMIN
                || (user.getRole() == UserRole.EDITOR && a.getStatus() != DRAFT);
        if (!ok) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xem bài này");
    }

    private NewsArticle find(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết"));
    }
}
