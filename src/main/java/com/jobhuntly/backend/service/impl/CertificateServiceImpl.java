package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.CertificateRequest;
import com.jobhuntly.backend.dto.response.CertificateResponse;
import com.jobhuntly.backend.entity.Certificate;
import com.jobhuntly.backend.entity.CandidateProfile;
import com.jobhuntly.backend.mapper.CertificateMapper;
import com.jobhuntly.backend.repository.CertificateRepository;
import com.jobhuntly.backend.service.CertificateService; 
import com.jobhuntly.backend.service.ProfileDomainService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {
    private final CertificateRepository repository;
    private final CertificateMapper mapper;
    private final ProfileDomainService profileDomainService;

    @Override
    @Transactional
    public CertificateResponse create(Long userId, CertificateRequest dto) {
        CandidateProfile profile = profileDomainService.getOrCreateProfile(userId);
        Certificate certificate = mapper.toEntity(dto);
        certificate.setProfile(profile);
        profile.getCertificates().add(certificate);
        return mapper.toResponseDTO(repository.save(certificate));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateResponse> getAll(Long userId) {
        return mapper.toResponseList(repository.findByProfileUserId(userId));
    }

    @Override
    @Transactional
    public CertificateResponse update(Long userId, Long id, CertificateRequest dto) {
        Certificate certificate = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        profileDomainService.checkOwnership(certificate.getProfile().getUser().getId(), userId);

        if (dto.getCerName() != null)
            certificate.setCerName(dto.getCerName());
        if (dto.getDate() != null)
            certificate.setDate(dto.getDate());
        if (dto.getDescription() != null)
            certificate.setDescription(dto.getDescription());
        if (dto.getIssuer() != null)
            certificate.setIssuer(dto.getIssuer());

        return mapper.toResponseDTO(repository.save(certificate));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        Certificate certificate = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
        profileDomainService.checkOwnership(certificate.getProfile().getUser().getId(), userId);
        repository.delete(certificate);
    }


}
