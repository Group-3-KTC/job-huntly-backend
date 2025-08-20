package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByParentIsNullAndNameIgnoreCase(String name);

    boolean existsByParentAndNameIgnoreCase(Category parent, String name);

    List<Category> findAllByParentIsNullOrderByNameAsc();

    List<Category> findAllByParent_NameIgnoreCaseOrderByNameAsc(String parentName);

    List<Category> findAllByParent_NameIgnoreCase(String parentName);

    List<Category> findAllByParent_Id(Long parentId);
}
