package com.minhhai.newsmanagement.repository;

import com.minhhai.newsmanagement.entity.NewsArticle;
import com.minhhai.newsmanagement.enums.ArticleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    List<NewsArticle> findByAuthorIdOrderByUpdatedAtDesc(Long authorId);
    long countByAuthorIdAndStatus(Long authorId, ArticleStatus status);
    long countByAuthorId(Long authorId);
    long countByCategoryId(Long categoryId);

    List<NewsArticle> findByStatusOrderBySubmittedAtAsc(ArticleStatus status);
    List<NewsArticle> findByStatusOrderByUpdatedAtDesc(ArticleStatus status);

    List<NewsArticle> findByStatusOrderByPublishedAtDesc(ArticleStatus status);
    List<NewsArticle> findByStatusAndCategoryIdOrderByPublishedAtDesc(ArticleStatus status, Long categoryId);

    @Query("select a.id from NewsArticle a where a.status = :status and a.scheduledPublishDate <= :now")
    List<Long> findDueIds(@Param("status") ArticleStatus status, @Param("now") Instant now);
}
