package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.CandidateSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, Long> {

    // Tìm theo userId (thông qua profile.user.id)
    List<CandidateSkill> findByProfileUserId(Long userId);

    // Tìm theo profileId và skillId - sử dụng tên đúng của field
    Optional<CandidateSkill> findByProfileProfileIdAndSkillId(Long profileId, Long skillId);

    // Kiểm tra tồn tại theo profileId và skillId
    boolean existsByProfileProfileIdAndSkillId(Long profileId, Long skillId);

    // Hoặc sử dụng @Query để rõ ràng hơn
    @Query("SELECT cs FROM CandidateSkill cs WHERE cs.profile.profileId = :profileId AND cs.skill.id = :skillId")
    Optional<CandidateSkill> findByProfileIdAndSkillId(@Param("profileId") Long profileId,
            @Param("skillId") Long skillId);

    @Query("SELECT COUNT(cs) > 0 FROM CandidateSkill cs WHERE cs.profile.profileId = :profileId AND cs.skill.id = :skillId")
    boolean existsByProfileIdAndSkillId(@Param("profileId") Long profileId, @Param("skillId") Long skillId);

    @Query("SELECT cs FROM CandidateSkill cs " +
            "JOIN cs.skill s " +
            "JOIN s.categories c " +
            "WHERE cs.profile.user.id = :userId AND c.id = :cateId")
    List<CandidateSkill> findByProfileUserIdAndCategoryId(@Param("userId") Long userId, @Param("cateId") Long cateId);
}