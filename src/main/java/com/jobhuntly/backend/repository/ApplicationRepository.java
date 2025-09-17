package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Application;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

	@EntityGraph(attributePaths = { "job", "job.company" })
	Page<Application> findAllByUser_Id(Long userId, Pageable pageable);

	Page<Application> findAllByJob_Id(Long jobId, Pageable pageable);

	// Lấy applications theo companyId (qua quan hệ job.company)
	@EntityGraph(attributePaths = { "job", "job.company", "user" })
	Page<Application> findAllByJob_Company_Id(Long companyId, Pageable pageable);

	boolean existsByUser_IdAndJob_Id(Long userId, Long jobId);

	Optional<Application> findByUser_IdAndJob_Id(Integer userId, Integer jobId);

	boolean existsByUser_IdAndJob_Id(Long userId, Integer jobId);

	Optional<Application> findByUser_IdAndJob_Id(Long userId, Long jobId);

	// Khóa ghi để tránh race condition khi user re-apply cùng lúc nhiều request
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from Application a where a.user.id = :userId and a.job.id = :jobId")
	Optional<Application> lockByUserAndJob(@Param("userId") Long userId, @Param("jobId") Long jobId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from Application a where a.id = :id")
	Optional<Application> lockById(@Param("id") Long id);

	// ---- Analytics ----
	@Query("select count(a) from Application a where a.job.company.id = :companyId and a.createdAt >= :from")
	long countByCompanyIdAndCreatedAtAfter(@Param("companyId") Long companyId, @Param("from") LocalDateTime from);

	@Query("select function('date', a.createdAt) as d, count(a) as c " +
			"from Application a " +
			"where a.job.company.id = :companyId and function('date', a.createdAt) between :fromDate and :toDate " +
			"group by function('date', a.createdAt) order by function('date', a.createdAt)")
	List<Object[]> countDailyByCompany(@Param("companyId") Long companyId,
										 @Param("fromDate") LocalDate from,
										 @Param("toDate") LocalDate to);

    @Query(value = """
      select job_id 
      from applications 
      where user_id = :uid and job_id in (:ids)
      """, nativeQuery = true)
    List<Long> findAppliedJobIdsIn(@Param("uid") Long userId, @Param("ids") Collection<Long> jobIds);

}
