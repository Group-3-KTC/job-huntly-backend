package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.response.ProfileCombinedResponse;
import com.jobhuntly.backend.dto.response.CandidateSkillResponse;
import com.jobhuntly.backend.entity.CandidateProfile;
import com.jobhuntly.backend.entity.CandidateSkill;
import com.jobhuntly.backend.entity.Category;
import com.jobhuntly.backend.mapper.ProfileMapper;
import com.jobhuntly.backend.repository.CandidateProfileRepository;
import com.jobhuntly.backend.service.ProfileDomainService;
import com.jobhuntly.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final ProfileMapper profileMapper;
    private final ProfileDomainService profileDomainService;

    @Override
    @Transactional
    public ProfileCombinedResponse getCombinedProfile(Long userId) {
        return candidateProfileRepository.findByUserIdWithAllRelations(userId)
                .map(profile -> {
                    ProfileCombinedResponse response = profileMapper.toCombinedResponse(profile);
                    // Fix category info cho candidateSkills
                    setCategoryInfoForCandidateSkills(response, profile);
                    return response;
                })
                .orElseGet(() -> {
                    CandidateProfile newProfile = profileDomainService.getOrCreateProfile(userId);
                    ProfileCombinedResponse response = profileMapper.toCombinedResponse(newProfile);
                    setCategoryInfoForCandidateSkills(response, newProfile);
                    return response;
                });
    }

    /**
     * Set category info cho list candidateSkills trong combined response
     * Tái sử dụng logic từ CandidateSkillServiceImpl.setCategoryInfo()
     */
    private void setCategoryInfoForCandidateSkills(ProfileCombinedResponse response, CandidateProfile profile) {
        if (response.getCandidateSkills() != null && profile.getCandidateSkills() != null) {
            // Tạo map để match DTO với entity dựa trên id
            var skillMap = profile.getCandidateSkills().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            skill -> skill.getId(),
                            skill -> skill));

            // Set category info cho từng skill response
            response.getCandidateSkills().forEach(skillResponse -> {
                CandidateSkill skill = skillMap.get(skillResponse.getId());
                if (skill != null) {
                    setCategoryInfo(skillResponse, skill);
                }
            });
        }
    }

    /**
     * Copy logic từ CandidateSkillServiceImpl.setCategoryInfo()
     */
    private void setCategoryInfo(CandidateSkillResponse response, CandidateSkill candidateSkill) {
        if (candidateSkill.getSkill() != null && candidateSkill.getSkill().getCategories() != null) {
            candidateSkill.getSkill().getCategories().stream()
                    .findFirst()
                    .ifPresent(category -> {
                        Category parentCategory = findTopLevelCategory(category);
                        response.setCategoryId(parentCategory.getId());
                        response.setCategoryName(parentCategory.getName());
                    });
        }
    }

    /**
     * Copy logic từ CandidateSkillServiceImpl.findTopLevelCategory()
     */
    private Category findTopLevelCategory(Category category) {
        Category current = category;
        while (current.getParent() != null) {
            current = current.getParent();
        }
        return current;
    }
}