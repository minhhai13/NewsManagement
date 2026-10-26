package com.minhhai.newsmanagement.dto;

import com.minhhai.newsmanagement.entity.WorkflowAuditLog;
import com.minhhai.newsmanagement.enums.ArticleStatus;
import com.minhhai.newsmanagement.enums.WorkflowAction;

import java.time.Instant;

public record AuditLogResponse(
        Long id, WorkflowAction action, ArticleStatus fromStatus, ArticleStatus toStatus,
        Long actorId, String actorName, String comment, Instant createdAt) {

    public static AuditLogResponse from(WorkflowAuditLog l) {
        return new AuditLogResponse(l.getId(), l.getAction(), l.getFromStatus(), l.getToStatus(),
                l.getActor() != null ? l.getActor().getId() : null,
                l.getActor() != null ? l.getActor().getName() : "Hệ thống (Scheduler)",
                l.getComment(), l.getCreatedAt());
    }
}
