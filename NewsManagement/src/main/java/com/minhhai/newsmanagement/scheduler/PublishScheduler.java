package com.minhhai.newsmanagement.scheduler;

import com.minhhai.newsmanagement.enums.ArticleStatus;
import com.minhhai.newsmanagement.repository.NewsArticleRepository;
import com.minhhai.newsmanagement.service.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Quét mỗi 10 giây; bài quá hạn (kể cả khi ứng dụng vừa khởi động lại) cũng được đăng. */
@Component
public class PublishScheduler {
    private static final Logger log = LoggerFactory.getLogger(PublishScheduler.class);

    private final NewsArticleRepository articleRepository;
    private final WorkflowService workflowService;

    public PublishScheduler(NewsArticleRepository articleRepository, WorkflowService workflowService) {
        this.articleRepository = articleRepository;
        this.workflowService = workflowService;
    }

    @Scheduled(fixedDelay = 10_000, initialDelay = 3_000)
    public void publishDueArticles() {
        for (Long id : articleRepository.findDueIds(ArticleStatus.APPROVED_SCHEDULED, Instant.now())) {
            try {
                if (workflowService.autoPublish(id)) log.info("Đã tự động đăng bài {}", id);
            } catch (Exception e) { // vd xung đột version với Editor: bỏ qua, lượt quét sau xử lý lại
                log.warn("Bỏ qua bài {} trong lượt quét này: {}", id, e.getMessage());
            }
        }
    }
}
