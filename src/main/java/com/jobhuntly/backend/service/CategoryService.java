package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.CategoryRequest;
import com.jobhuntly.backend.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    void createCategory(CategoryRequest categoryRequest);
    List<CategoryResponse> getAllCategories();
}
