package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.CategoryRequest;
import com.jobhuntly.backend.dto.response.CategoryResponse;
import com.jobhuntly.backend.entity.Category;
import com.jobhuntly.backend.mapper.CategoryMapper;
import com.jobhuntly.backend.repository.CategoryRepository;
import com.jobhuntly.backend.service.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.jobhuntly.backend.constant.CacheConstant.DICT_CATEGORIES;

@Service
@AllArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        String name = normalize(categoryRequest.getName());
        String parentName = normalize(categoryRequest.getParentName());

        if (name.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }

        // Category root
        if (parentName.isBlank()) {
            if (categoryRepository.existsByParentIsNullAndNameIgnoreCase(name)) {
                throw new IllegalArgumentException("Category already exists");
            }
            Category root = categoryMapper.toEntity(categoryRequest);
            root.setParent(null);
            Category saved = categoryRepository.save(root);
            return categoryMapper.toResponse(saved);
        }
        Category children = categoryRepository.findByNameIgnoreCase(parentName)
                .orElseThrow(() -> new IllegalArgumentException("Parent Category not found " +parentName));

        if (categoryRepository.existsByParentAndNameIgnoreCase(children, name)) {
            throw new IllegalArgumentException("Category already exists ");
        }
        Category child = categoryMapper.toEntity(categoryRequest);
        child.setParent(children);

        Category saved = categoryRepository.save(child);
        return categoryMapper.toResponse(saved);
    }

    @Override
    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findAllByParentIsNullOrderByNameAsc().stream()
                .map(categoryMapper::toResponse).toList();
    }

    @Override
    public List<CategoryResponse> getChildrenByParentName(String parentName) {
        String parent = normalize(parentName);
        if (parent.isBlank()) {
            throw new IllegalArgumentException("Parent name is required");
        }
        categoryRepository.findByNameIgnoreCase(parent)
                .orElseThrow(() -> new IllegalArgumentException("Parent category not found" +parent));

        return categoryRepository.findAllByParent_NameIgnoreCaseOrderByNameAsc(parent)
                .stream().map(categoryMapper::toResponse).toList();
    }

    @Override
    @Cacheable(cacheNames = DICT_CATEGORIES, key = "'all'", sync = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getChildrenByParentId(Long parentId) {
        if (parentId == null) {
            throw new IllegalArgumentException("Parent ID is required");
        }

        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent category not found with ID: " + parentId));

        return parent.getChildren().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    private String normalize(String raw) {
        return raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
    }
}
