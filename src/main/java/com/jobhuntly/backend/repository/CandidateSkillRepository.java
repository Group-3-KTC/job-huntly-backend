package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.CandidateSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, Long> {


    List<CandidateSkill> findByProfileUserId(Long userId);


    Optional<CandidateSkill> findByProfileProfileIdAndSkillId(Long profileId, Long skillId);

    boolean existsByProfileProfileIdAndSkillId(Long profileId, Long skillId);

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