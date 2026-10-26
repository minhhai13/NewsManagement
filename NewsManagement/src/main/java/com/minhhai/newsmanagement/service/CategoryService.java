package com.minhhai.newsmanagement.service;

import com.minhhai.newsmanagement.dto.CategoryRequest;
import com.minhhai.newsmanagement.dto.CategoryResponse;
import com.minhhai.newsmanagement.entity.Category;
import com.minhhai.newsmanagement.repository.CategoryRepository;
import com.minhhai.newsmanagement.repository.NewsArticleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final NewsArticleRepository articleRepository;

    public CategoryService(CategoryRepository categoryRepository, NewsArticleRepository articleRepository) {
        this.categoryRepository = categoryRepository;
        this.articleRepository = articleRepository;
    }

    /** Admin thấy tất cả; người khác chỉ thấy danh mục đang hoạt động. */
    @Transactional(readOnly = true)
    public List<CategoryResponse> list(boolean includeInactive) {
        List<Category> list = includeInactive ? categoryRepository.findAll() : categoryRepository.findByStatus(1);
        return list.stream().map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(Long id) {
        return CategoryResponse.from(find(id));
    }

    public CategoryResponse create(CategoryRequest req) {
        Category c = new Category();
        apply(c, req);
        return CategoryResponse.from(categoryRepository.save(c));
    }

    public CategoryResponse update(Long id, CategoryRequest req) {
        Category c = find(id);
        if (req.parentCategoryId() != null && req.parentCategoryId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh mục không thể là cha của chính nó");
        }
        apply(c, req);
        return CategoryResponse.from(categoryRepository.save(c));
    }

    /** Đang được dùng (có bài viết hoặc danh mục con) thì vô hiệu hóa, chưa dùng thì xóa hẳn. */
    public String remove(Long id) {
        Category c = find(id);
        if (articleRepository.countByCategoryId(id) > 0 || categoryRepository.existsByParentCategoryId(id)) {
            c.setStatus(0);
            categoryRepository.save(c);
            return "Danh mục đang được sử dụng nên được vô hiệu hóa thay vì xóa";
        }
        categoryRepository.delete(c);
        return "Đã xóa danh mục";
    }

    private void apply(Category c, CategoryRequest req) {
        c.setName(req.name().trim());
        c.setDescription(req.description());
        c.setStatus(req.status() == null ? 1 : req.status());
        c.setParentCategory(req.parentCategoryId() == null ? null : find(req.parentCategoryId()));
    }

    private Category find(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"));
    }
}
