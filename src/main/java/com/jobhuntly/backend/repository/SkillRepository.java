package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    List<Skill> findDistinctByCategories_NameIgnoreCase(String categoryName);

    List<Skill> findDistinctByCategories_NameIn(Collection<String> categoryNames);

    @Query("""
        select distinct s.name
        from Skill s
        join s.jobs j
        where j.id = :jobId
        order by s.name
    """)
    List<String> findNamesByJobId(@Param("jobId") Long jobId);
}
