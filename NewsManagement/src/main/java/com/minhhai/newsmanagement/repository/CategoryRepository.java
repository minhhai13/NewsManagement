package com.minhhai.newsmanagement.repository;

import com.minhhai.newsmanagement.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByStatus(Integer status);
    boolean existsByParentCategoryId(Long parentId);
}
