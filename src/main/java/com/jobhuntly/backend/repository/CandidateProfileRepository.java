package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {
    
    Optional<CandidateProfile> findByUser_Id(Long userId);
    
    @Query("SELECT cp FROM CandidateProfile cp " +
            "LEFT JOIN FETCH cp.user " +
            "LEFT JOIN FETCH cp.workExperiences " +
            "LEFT JOIN FETCH cp.educations " +
            "LEFT JOIN FETCH cp.certificates " +
            "LEFT JOIN FETCH cp.awards " +
            "LEFT JOIN FETCH cp.softSkills " +
            "LEFT JOIN FETCH cp.candidateSkills cs " +
            "LEFT JOIN FETCH cs.skill s " +
            "LEFT JOIN FETCH cs.level " +
            "WHERE cp.user.id = :userId")
    Optional<CandidateProfile> findByUserIdWithAllRelations(@Param("userId") Long userId);

}