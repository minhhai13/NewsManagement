package com.minhhai.newsmanagement.controller;

import com.minhhai.newsmanagement.dto.ArticleResponse;
import com.minhhai.newsmanagement.service.NewsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/news")
public class PublicNewsController {

    private final NewsService newsService;

    public PublicNewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public List<ArticleResponse> list(@RequestParam(required = false) Long categoryId) {
        return newsService.publicList(categoryId);
    }

    @GetMapping("/{id}")
    public ArticleResponse detail(@PathVariable Long id) {
        return newsService.publicDetail(id);
    }
}
