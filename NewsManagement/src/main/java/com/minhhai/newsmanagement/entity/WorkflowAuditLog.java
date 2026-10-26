package com.minhhai.newsmanagement.entity;

import com.minhhai.newsmanagement.enums.ArticleStatus;
import com.minhhai.newsmanagement.enums.WorkflowAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "workflow_audit_log")
@Getter @NoArgsConstructor
public class WorkflowAuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false, updatable = false)
    private NewsArticle article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", updatable = false)
    private UserAccount actor; // null = Scheduler / SYSTEM

    @Enumerated(EnumType.STRING)
    @Column(length = 30, updatable = false)
    private ArticleStatus fromStatus; // null khi tạo Draft

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, updatable = false)
    private ArticleStatus toStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40, updatable = false)
    private WorkflowAction action;

    @Column(length = Integer.MAX_VALUE, updatable = false)
    private String comment;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public WorkflowAuditLog(NewsArticle article, UserAccount actor,
            ArticleStatus from, ArticleStatus to,
            WorkflowAction action, String comment) {
        this.article = article;
        this.actor = actor;
        this.fromStatus = from;
        this.toStatus = to;
        this.action = action;
        this.comment = comment;
        this.createdAt = Instant.now();
    }
}
