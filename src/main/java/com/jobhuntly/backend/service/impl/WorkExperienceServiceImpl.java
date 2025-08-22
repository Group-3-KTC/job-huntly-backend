// package com.jobhuntly.backend.service.impl;

// import com.jobhuntly.backend.dto.request.WorkExperienceRequest;
// import com.jobhuntly.backend.dto.response.WorkExperienceResponse;
// import com.jobhuntly.backend.entity.WorkExperience;
// import com.jobhuntly.backend.entity.CandidateProfile;
// import com.jobhuntly.backend.mapper.WorkExperienceMapper;
// import com.jobhuntly.backend.repository.WorkExperienceRepository;
// import com.jobhuntly.backend.service.WorkExperienceService;
// import com.jobhuntly.backend.repository.CandidateProfileRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.List;

// @Service
// @RequiredArgsConstructor
// public class WorkExperienceServiceImpl implements WorkExperienceService {
//     private final WorkExperienceRepository repository;
//     private final CandidateProfileRepository profileRepository;
//     private final WorkExperienceMapper mapper;

//     @Override
//     @Transactional
//     public WorkExperienceResponse create(Long userId, WorkExperienceRequest dto) {
//         CandidateProfile profile = getOrCreateProfile(userId);
//         WorkExperience experience = mapper.toEntity(dto);
//         experience.setProfile(profile);
//         profile.getWorkExperiences().add(experience);
//         return mapper.toResponseDTO(repository.save(experience));
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public List<WorkExperienceResponse> getAll(Long userId) {
//         List<WorkExperience> experiences = repository.findByProfileUserId(userId);
//         return mapper.toResponseList(experiences);
//     }

//     @Override
//     @Transactional
//     public WorkExperienceResponse update(Long userId, Long id, WorkExperienceRequest dto) {
//         WorkExperience experience = repository.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Work Experience not found"));
//         checkOwnership(experience.getProfile().getUser().getId(), userId);

//         if (dto.getDescription() != null)
//             experience.setDescription(dto.getDescription());
//         if (dto.getCompanyName() != null)
//             experience.setCompanyName(dto.getCompanyName());
//         if (dto.getPosition() != null)
//             experience.setPosition(dto.getPosition());
//         if (dto.getDuration() != null)
//             experience.setDuration(dto.getDuration());

//         return mapper.toResponseDTO(repository.save(experience));
//     }

//     @Override
//     @Transactional
//     public void delete(Long userId, Long id) {
//         WorkExperience experience = repository.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Work Experience not found"));
//         checkOwnership(experience.getProfile().getUser().getId(), userId);
//         repository.delete(experience);
//     }

//     private CandidateProfile getOrCreateProfile(Long userId) {
//         return profileRepository.findByUser_Id(userId)
//                 .orElseThrow(() -> new RuntimeException("Profile not found"));
//     }

//     private void checkOwnership(Long ownerId, Long currentUserId) {
//         if (!ownerId.equals(currentUserId)) {
//             throw new RuntimeException("Unauthorized access");
//         }
//     }
// }
package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.WorkExperienceRequest;
import com.jobhuntly.backend.dto.response.WorkExperienceResponse;
import com.jobhuntly.backend.entity.WorkExperience;
import com.jobhuntly.backend.entity.CandidateProfile;
import com.jobhuntly.backend.exception.ResourceNotFoundException;
import com.jobhuntly.backend.mapper.WorkExperienceMapper;
import com.jobhuntly.backend.repository.WorkExperienceRepository;
import com.jobhuntly.backend.service.WorkExperienceService;
import com.jobhuntly.backend.service.ProfileDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkExperienceServiceImpl implements WorkExperienceService {

    private final WorkExperienceRepository repository;
    private final WorkExperienceMapper mapper;
    private final ProfileDomainService profileDomainService;

    @Override
    @Transactional
    public WorkExperienceResponse create(Long userId, WorkExperienceRequest dto) {
        CandidateProfile profile = profileDomainService.getOrCreateProfile(userId);
        WorkExperience experience = mapper.toEntity(dto);
        experience.setProfile(profile);
        profile.getWorkExperiences().add(experience);
        return mapper.toResponseDTO(repository.save(experience));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkExperienceResponse> getAll(Long userId) {
        return mapper.toResponseList(repository.findByProfileUserId(userId));
    }

    @Override
    @Transactional
    public WorkExperienceResponse update(Long userId, Long id, WorkExperienceRequest dto) {
        WorkExperience experience = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work Experience not found"));

        profileDomainService.checkOwnership(experience.getProfile().getUser().getId(), userId);

        if (dto.getDescription() != null)
            experience.setDescription(dto.getDescription());
        if (dto.getCompanyName() != null)
            experience.setCompanyName(dto.getCompanyName());
        if (dto.getPosition() != null)
            experience.setPosition(dto.getPosition());
        if (dto.getDuration() != null)
            experience.setDuration(dto.getDuration());

        return mapper.toResponseDTO(repository.save(experience));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        WorkExperience experience = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work Experience not found"));

        profileDomainService.checkOwnership(experience.getProfile().getUser().getId(), userId);

        repository.delete(experience);
    }
}
