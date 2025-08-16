package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    @EntityGraph(attributePaths = {
            "company", "categories", "skills", "levels", "workTypes",
            "wards", "wards.city"
    })
    Optional<Job> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"company", "skills"})
    Page<Job> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"company"})
    Page<Job> findAllByCompany_Id(Long companyId, Pageable pageable);

    // filter theo mức lương
    @Query("""
        select distinct j from Job j
        where j.salaryType = 0
          and j.salaryMin <= :max
          and j.salaryMax >= :min
    """)
    List<Job> findBySalaryOverlap(@Param("min") long min, @Param("max") long max);

    List<Job> findByStatusIgnoreCase(String status);

    Page<Job> findByCompany_Id(Long companyId, Pageable pageable);

    default Optional<Job> findByIdWithAssociations(Long id) {
        return findById(id);
    }

    default Page<Job> findByCompanyIdWithAssociations(Long companyId, Pageable pageable) {
        return findByCompany_Id(companyId, pageable);
    }

    @Query("SELECT COUNT(j) FROM Job j WHERE j.company.id = :companyId")
    long countJobsByCompanyId(@Param("companyId") Long companyId);

    @EntityGraph(attributePaths = {"company", "skills"})
    List<Job> findByIdIn(List<Long> ids);
}
