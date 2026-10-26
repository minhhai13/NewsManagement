package com.minhhai.newsmanagement.controller;

import com.minhhai.newsmanagement.dto.ArticleResponse;
import com.minhhai.newsmanagement.dto.WorkflowRequests;
import com.minhhai.newsmanagement.security.CurrentUser;
import com.minhhai.newsmanagement.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final WorkflowService workflowService;

    public AdminController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/news/{id}/override")
    public ArticleResponse override(@PathVariable Long id, @Valid @RequestBody WorkflowRequests.Override body) {
        return ArticleResponse.from(workflowService.override(id, CurrentUser.get(),
                body.targetStatus(), body.reason(), body.scheduledPublishDate()));
    }
}
