package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.EduRequest;
import com.jobhuntly.backend.dto.response.EduResponse;
import com.jobhuntly.backend.entity.Edu;
import com.jobhuntly.backend.entity.CandidateProfile;
import com.jobhuntly.backend.mapper.EduMapper;
import com.jobhuntly.backend.repository.EduRepository;
import com.jobhuntly.backend.service.EduService;
import com.jobhuntly.backend.service.ProfileDomainService;
import com.jobhuntly.backend.repository.CandidateProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EduServiceImpl implements EduService {
    private final EduRepository repository;
    private final EduMapper mapper;
    private final ProfileDomainService profileDomainService;

    @Override
    @Transactional
    public EduResponse create(Long userId, EduRequest dto) {
        CandidateProfile profile = profileDomainService.getOrCreateProfile(userId);
        Edu edu = mapper.toEntity(dto);
        edu.setProfile(profile);
        profile.getEducations().add(edu);
        return mapper.toResponseDTO(repository.save(edu));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EduResponse> getAll(Long userId) {
        return mapper.toResponseList(repository.findByProfileUserId(userId));
    }

    @Override
    @Transactional
    public EduResponse update(Long userId, Long id, EduRequest dto) {
        Edu edu = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Education not found"));

        profileDomainService.checkOwnership(edu.getProfile().getUser().getId(), userId);

        if (dto.getSchoolName() != null)
            edu.setSchoolName(dto.getSchoolName());
        if (dto.getDegree() != null)
            edu.setDegree(dto.getDegree());
        if (dto.getDuration() != null)
            edu.setDuration(dto.getDuration());
        if (dto.getMajors() != null)
            edu.setMajors(dto.getMajors());

        return mapper.toResponseDTO(repository.save(edu));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        Edu edu = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Education not found"));
        profileDomainService.checkOwnership(edu.getProfile().getUser().getId(), userId);
        repository.delete(edu);
    }


}
