package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.CategoryRequest;
import com.jobhuntly.backend.dto.response.CategoryResponse;
import com.jobhuntly.backend.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
    Category toEntity(CategoryRequest categoryRequest);

}
