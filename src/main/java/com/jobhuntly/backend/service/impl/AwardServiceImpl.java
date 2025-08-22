package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.AwardRequest;
import com.jobhuntly.backend.dto.response.AwardResponse;
import com.jobhuntly.backend.entity.Award;
import com.jobhuntly.backend.entity.CandidateProfile;
import com.jobhuntly.backend.exception.ResourceNotFoundException;
import com.jobhuntly.backend.mapper.AwardMapper;
import com.jobhuntly.backend.repository.AwardRepository;
import com.jobhuntly.backend.service.AwardService;
import com.jobhuntly.backend.service.ProfileDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AwardServiceImpl implements AwardService {

    private final AwardRepository repository;
    private final AwardMapper mapper;
    private final ProfileDomainService profileDomainService; 

    @Override
    @Transactional
    public AwardResponse create(Long userId, AwardRequest dto) {
        CandidateProfile profile = profileDomainService.getProfileOrThrow(userId); 
        Award award = mapper.toEntity(dto);
        award.setProfile(profile);
        profile.getAwards().add(award);
        return mapper.toResponseDTO(repository.save(award));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AwardResponse> getAll(Long userId) {
        return mapper.toResponseList(repository.findByProfileUserId(userId));
    }

    @Override
    @Transactional
    public AwardResponse update(Long userId, Long id, AwardRequest dto) {
        Award award = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Award not found"));

        profileDomainService.checkOwnership(award.getProfile().getUser().getId(), userId);

        if (dto.getName() != null)
            award.setName(dto.getName());
        if (dto.getIssuer() != null)
            award.setIssuer(dto.getIssuer());
        if (dto.getDate() != null)
            award.setDate(dto.getDate());
        if (dto.getDescription() != null)
            award.setDescription(dto.getDescription());

        return mapper.toResponseDTO(repository.save(award));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        Award award = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Award not found"));

        profileDomainService.checkOwnership(award.getProfile().getUser().getId(), userId);
        repository.delete(award);
    }


}
