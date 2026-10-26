package com.minhhai.newsmanagement.controller;

import com.minhhai.newsmanagement.dto.AuditLogResponse;
import com.minhhai.newsmanagement.security.CurrentUser;
import com.minhhai.newsmanagement.service.EditorialService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Lịch sử workflow của một bài: tác giả xem bài mình, Editor/Admin xem theo quyền. */
@RestController
@RequestMapping("/api/v1/news")
public class AuditController {

    private final EditorialService editorialService;

    public AuditController(EditorialService editorialService) {
        this.editorialService = editorialService;
    }

    @GetMapping("/{id}/audit-logs")
    public List<AuditLogResponse> auditLogs(@PathVariable Long id) {
        return editorialService.auditLogs(id, CurrentUser.get());
    }
}
