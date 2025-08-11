package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.CategoryRequest;
import com.jobhuntly.backend.dto.response.CategoryResponse;
import com.jobhuntly.backend.entity.Category;
import com.jobhuntly.backend.mapper.CategoryMapper;
import com.jobhuntly.backend.repository.CategoryRepository;
import com.jobhuntly.backend.service.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    @Override
    public void createCategory(CategoryRequest categoryRequest) {
        Optional.ofNullable(categoryRepository.findByNameIgnoreCase(categoryRequest.getName()))
                .ifPresentOrElse(existing -> {
                    throw new IllegalArgumentException("Category already exists");
                }, () -> {
                            Category newCategory = categoryMapper.toEntity(categoryRequest);
                            categoryRepository.save(newCategory);
                        }
                        );
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }
}
