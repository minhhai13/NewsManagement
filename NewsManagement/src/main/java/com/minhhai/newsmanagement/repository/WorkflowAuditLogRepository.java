package com.minhhai.newsmanagement.repository;

import com.minhhai.newsmanagement.entity.WorkflowAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowAuditLogRepository extends JpaRepository<WorkflowAuditLog, Long> {
    List<WorkflowAuditLog> findByArticleIdOrderByCreatedAtAscIdAsc(Long articleId);
}
