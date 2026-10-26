package com.minhhai.newsmanagement.controller;

import com.minhhai.newsmanagement.dto.CategoryRequest;
import com.minhhai.newsmanagement.dto.CategoryResponse;
import com.minhhai.newsmanagement.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> list(Authentication auth) {
        boolean admin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return categoryService.list(admin);
    }

    @GetMapping("/{id}")
    public CategoryResponse get(@PathVariable Long id) {
        return categoryService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        return Map.of("message", categoryService.remove(id));
    }
}
