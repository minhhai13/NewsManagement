package com.minhhai.newsmanagement.dto;

import com.minhhai.newsmanagement.entity.NewsArticle;
import com.minhhai.newsmanagement.enums.ArticleStatus;

import java.time.Instant;

public record ArticleResponse(
        Long id, String title, String headline, String content, String tags,
        ArticleStatus status,
        Long categoryId, String categoryName,
        Long authorId, String authorName,
        String editorFeedback,
        Instant scheduledPublishDate, Instant publishedAt, Instant submittedAt,
        Instant createdAt, Instant updatedAt, Long version) {

    public static ArticleResponse from(NewsArticle a) {
        return new ArticleResponse(
                a.getId(), a.getTitle(), a.getHeadline(), a.getContent(), a.getTags(), a.getStatus(),
                a.getCategory() != null ? a.getCategory().getId() : null,
                a.getCategory() != null ? a.getCategory().getName() : null,
                a.getAuthor().getId(), a.getAuthor().getName(),
                a.getEditorFeedback(),
                a.getScheduledPublishDate(), a.getPublishedAt(), a.getSubmittedAt(),
                a.getCreatedAt(), a.getUpdatedAt(), a.getVersion());
    }
}
