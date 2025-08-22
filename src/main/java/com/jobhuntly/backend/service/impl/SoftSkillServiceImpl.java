package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.SoftSkillRequest;
import com.jobhuntly.backend.dto.response.SoftSkillResponse;
import com.jobhuntly.backend.entity.CandidateProfile;
import com.jobhuntly.backend.entity.SoftSkill;
import com.jobhuntly.backend.exception.ResourceNotFoundException;
import com.jobhuntly.backend.mapper.SoftSkillMapper;
import com.jobhuntly.backend.repository.SoftSkillRepository;
import com.jobhuntly.backend.service.ProfileDomainService;
import com.jobhuntly.backend.service.SoftSkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jobhuntly.backend.entity.enums.Level;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SoftSkillServiceImpl implements SoftSkillService {
    private final SoftSkillRepository repository;
    private final SoftSkillMapper mapper;
    private final ProfileDomainService profileDomainService;

    @Override
    @Transactional
    public SoftSkillResponse create(Long userId, SoftSkillRequest dto) {
        CandidateProfile profile = profileDomainService.getProfileOrThrow(userId);
        SoftSkill softSkill = mapper.toEntity(dto);
        softSkill.setProfile(profile);
        profile.getSoftSkills().add(softSkill);
        return mapper.toResponseDTO(repository.save(softSkill));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SoftSkillResponse> getAll(Long userId) {
        return mapper.toResponseList(repository.findByProfileUserId(userId));
    }

    @Override
    @Transactional
    public SoftSkillResponse update(Long userId, Long id, SoftSkillRequest dto) {
        SoftSkill softSkill = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SoftSkill not found"));
        profileDomainService.checkOwnership(softSkill.getProfile().getUser().getId(), userId);
        if (dto.getName() != null)
            softSkill.setName(dto.getName());
        if (dto.getDescription() != null)
            softSkill.setDescription(dto.getDescription());
        if (dto.getLevel() != null)
            softSkill.setLevel(Level.valueOf(dto.getLevel()));
        return mapper.toResponseDTO(repository.save(softSkill));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        SoftSkill softSkill = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SoftSkill not found"));
        profileDomainService.checkOwnership(softSkill.getProfile().getUser().getId(), userId);
        repository.delete(softSkill);
    }
}