package com.minhhai.newsmanagement.controller;

import com.minhhai.newsmanagement.dto.ArticleResponse;
import com.minhhai.newsmanagement.dto.WorkflowRequests;
import com.minhhai.newsmanagement.enums.ArticleStatus;
import com.minhhai.newsmanagement.security.CurrentUser;
import com.minhhai.newsmanagement.service.EditorialService;
import com.minhhai.newsmanagement.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** API của Editor (và Admin khi gỡ bài / xem danh sách). */
@RestController
@RequestMapping("/api/v1/editorial")
public class EditorialController {

    private final EditorialService editorialService;
    private final WorkflowService workflowService;

    public EditorialController(EditorialService editorialService, WorkflowService workflowService) {
        this.editorialService = editorialService;
        this.workflowService = workflowService;
    }

    @GetMapping("/queue")
    public List<ArticleResponse> queue() {
        return editorialService.queue();
    }

    @GetMapping("/articles")
    public List<ArticleResponse> articles(@RequestParam ArticleStatus status) {
        return editorialService.listByStatus(status, CurrentUser.get());
    }

    @GetMapping("/{id}")
    public ArticleResponse detail(@PathVariable Long id) {
        return editorialService.editorDetail(id, CurrentUser.get());
    }

    @PostMapping("/{id}/request-changes")
    public ArticleResponse requestChanges(@PathVariable Long id, @Valid @RequestBody WorkflowRequests.RequestChanges body) {
        return ArticleResponse.from(workflowService.requestChanges(id, CurrentUser.get(), body.feedback()));
    }

    @PostMapping("/{id}/approve")
    public ArticleResponse approve(@PathVariable Long id, @RequestBody(required = false) WorkflowRequests.Approve body) {
        return ArticleResponse.from(workflowService.approve(id, CurrentUser.get(),
                body == null ? null : body.scheduledPublishDate()));
    }

    @PostMapping("/{id}/archive")
    public ArticleResponse archive(@PathVariable Long id, @RequestBody(required = false) WorkflowRequests.Archive body) {
        return ArticleResponse.from(workflowService.archive(id, CurrentUser.get(), body == null ? null : body.reason()));
    }
}
