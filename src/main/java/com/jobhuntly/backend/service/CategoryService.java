package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.CategoryRequest;
import com.jobhuntly.backend.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest categoryRequest);

    List<CategoryResponse> getRootCategories();

    List<CategoryResponse> getChildrenByParentName(String parentName);

    List<CategoryResponse> getAllCategories();

    List<CategoryResponse> getChildrenByParentId(Long parentId);
}
