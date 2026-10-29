package com.minhhai.newsmanagement.controller;

import com.minhhai.newsmanagement.dto.ArticleRequest;
import com.minhhai.newsmanagement.dto.ArticleResponse;
import com.minhhai.newsmanagement.entity.UserAccount;
import com.minhhai.newsmanagement.security.CurrentUser;
import com.minhhai.newsmanagement.service.NewsService;
import com.minhhai.newsmanagement.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** API của Contributor (tác giả). */
@RestController
@RequestMapping("/api/v1/news")
public class NewsController {

    private final NewsService newsService;
    private final WorkflowService workflowService;

    public NewsController(NewsService newsService, WorkflowService workflowService) {
        this.newsService = newsService;
        this.workflowService = workflowService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponse create(@Valid @RequestBody ArticleRequest request) {
        return newsService.createDraft(request, CurrentUser.get());
    }

    @PutMapping("/{id}")
    public ArticleResponse update(@PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        return newsService.update(id, request, CurrentUser.get());
    }

    @GetMapping("/my")
    public List<ArticleResponse> my() {
        return newsService.myArticles(CurrentUser.get());
    }

    @GetMapping("/my/changes-count")
    public Map<String, Long> changesCount() {
        return Map.of("count", newsService.changesCount(CurrentUser.get()));
    }

    @GetMapping("/{id}")
    public ArticleResponse get(@PathVariable Long id) {
        return newsService.myArticle(id, CurrentUser.get());
    }

    @PostMapping("/{id}/submit")
    public ArticleResponse submit(@PathVariable Long id) {
        UserAccount user = CurrentUser.get();
        return ArticleResponse.from(workflowService.submit(id, user));
    }

    @PostMapping("/{id}/resubmit")
    public ArticleResponse resubmit(@PathVariable Long id) {
        UserAccount user = CurrentUser.get();
        return ArticleResponse.from(workflowService.resubmit(id, user));
    }
}
