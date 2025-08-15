package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.response.CompanyDto;
import com.jobhuntly.backend.entity.Category;
import com.jobhuntly.backend.entity.Company;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "categories", expression = "java(mapCategoryNames(company.getCategories()))")
    @Mapping(target = "parentCategories", expression = "java(mapParentCategoryNames(company.getCategories()))")
    @Mapping(target = "categoryIds", ignore = true)
    CompanyDto toDto(Company company);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "categories", ignore = true)
    Company toEntity(CompanyDto dto);

    List<CompanyDto> toDtoList(List<Company> companies);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "categories", ignore = true)
    void updateEntityFromDto(CompanyDto dto, @MappingTarget Company entity);

    // ===== Helpers =====
    default Set<String> mapCategoryNames(Set<Category> categories) {
        if (categories == null || categories.isEmpty()) return Collections.emptySet();
        return categories.stream()
                .map(Category::getName)
                .collect(Collectors.toSet());
    }

    default Set<String> mapParentCategoryNames(Set<Category> categories) {
        if (categories == null || categories.isEmpty()) return Collections.emptySet();
        return categories.stream()
                .map(Category::getParent)       // lấy category cha
                .filter(Objects::nonNull)       // bỏ null
                .map(Category::getName)     // lấy tên cha
                .collect(Collectors.toSet());
    }

//    toDto → Dùng khi đọc từ DB trả ra API.
//    toEntity → Dùng khi tạo mới từ dữ liệu client gửi lên.
//    toDtoList → Dùng khi trả danh sách công ty.
//    updateEntityFromDto → Dùng khi update một phần dữ liệu.
//    mapCategoryNames → Chuyển đổi dữ liệu category từ entity sang tên ngành.
}