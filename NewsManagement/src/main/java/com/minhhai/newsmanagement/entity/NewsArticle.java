package com.minhhai.newsmanagement.entity;

import com.minhhai.newsmanagement.enums.ArticleStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "news_articles")
@Getter @Setter @NoArgsConstructor
public class NewsArticle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 500)
    private String headline;

    @Column(length = Integer.MAX_VALUE) // nvarchar(max); DRAFT được phép chưa có nội dung
    private String content;

    @Column(length = 1000)
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ArticleStatus status = ArticleStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id") // bắt buộc khi gửi duyệt, DRAFT có thể chưa chọn
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private UserAccount author;

    @Column(name = "editor_feedback", length = Integer.MAX_VALUE)
    private String editorFeedback;

    private Instant scheduledPublishDate;
    private Instant publishedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private Instant submittedAt;

    @Version
    private Long version;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }
}
