package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    List<Skill> findDistinctByCategories_NameIgnoreCase(String categoryName);

    List<Skill> findDistinctByCategories_NameIn(Collection<String> categoryNames);
}
