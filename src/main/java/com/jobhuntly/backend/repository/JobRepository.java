package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Application;
import com.jobhuntly.backend.entity.Job;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    // --- Maintenance: set inactive cho job quá hạn ---
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update Job j set j.status = 'inactive' where j.expiredDate < CURRENT_DATE and lower(j.status) <> 'inactive'")
    int markExpiredJobsInactive();


    // lấy companyId từ jobId (dùng để verify job thuộc company khi tạo interview)
    @Query("select j.company.id from Job j where j.id = :jobId")
    Optional<Long> findCompanyIdByJobId(@Param("jobId") Long jobId);
}
