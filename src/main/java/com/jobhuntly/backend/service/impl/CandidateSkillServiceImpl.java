package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.CandidateSkillRequest;
import com.jobhuntly.backend.dto.response.CandidateSkillResponse;
import com.jobhuntly.backend.entity.*;
import com.jobhuntly.backend.exception.ResourceNotFoundException;
import com.jobhuntly.backend.mapper.CandidateSkillMapper;
import com.jobhuntly.backend.repository.CandidateSkillRepository;
import com.jobhuntly.backend.repository.LevelRepository;
import com.jobhuntly.backend.repository.SkillRepository;
import com.jobhuntly.backend.service.CandidateSkillService;
import com.jobhuntly.backend.service.ProfileDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CandidateSkillServiceImpl implements CandidateSkillService {
    private final CandidateSkillRepository repository;
    private final SkillRepository skillRepository;
    private final LevelRepository levelRepository;
    private final CandidateSkillMapper mapper;
    private final ProfileDomainService profileDomainService;

    @Override
    @Transactional
    public CandidateSkillResponse create(Long userId, CandidateSkillRequest dto) {
        // Kiểm tra hồ sơ ứng viên
        CandidateProfile profile = profileDomainService.getProfileOrThrow(userId);

        // Kiểm tra kỹ năng tồn tại
        Skill skill = skillRepository.findById(dto.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kỹ năng với ID: " + dto.getSkillId()));

        // Kiểm tra mức độ kỹ năng (nếu có)
        Level level = null;
        if (dto.getLevelId() != null) {
            level = levelRepository.findById(dto.getLevelId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy mức độ kỹ năng với ID: " + dto.getLevelId()));
        }

        // Kiểm tra xem skill đã tồn tại trong profile chưa
        Optional<CandidateSkill> existingSkill = repository.findByProfileIdAndSkillId(
                profile.getProfileId(), dto.getSkillId());

        CandidateSkill candidateSkill;
        if (existingSkill.isPresent()) {
            // Cập nhật level nếu skill đã tồn tại
            candidateSkill = existingSkill.get();
            candidateSkill.setLevel(level);
        } else {
            // Tạo mới
            candidateSkill = new CandidateSkill();
            candidateSkill.setProfile(profile);
            candidateSkill.setSkill(skill);
            candidateSkill.setLevel(level);
        }

        // Lưu
        CandidateSkill savedSkill = repository.save(candidateSkill);

        // Tạo response
        CandidateSkillResponse response = mapper.toResponseDTO(savedSkill);
        setCategoryInfo(response, skill);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateSkillResponse> getAll(Long userId) {
        List<CandidateSkill> skills = repository.findByProfileUserId(userId);
        List<CandidateSkillResponse> responses = mapper.toResponseList(skills);
        responses.forEach(response -> {
            Skill skill = skillRepository.findById(response.getSkillId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy kỹ năng với ID: " + response.getSkillId()));
            setCategoryInfo(response, skill);
        });
        return responses;
    }

    @Override
    @Transactional
    public CandidateSkillResponse update(Long userId, Long candidateSkillId, CandidateSkillRequest dto) {
        // Tìm CandidateSkill by ID
        CandidateSkill candidateSkill = repository.findById(candidateSkillId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy CandidateSkill với ID: " + candidateSkillId));

        // Kiểm tra quyền sở hữu
        profileDomainService.checkOwnership(candidateSkill.getProfile().getUser().getId(), userId);

        // Cập nhật skill nếu được cung cấp
        if (dto.getSkillId() != null) {
            // Kiểm tra trùng lặp với skill khác trong cùng profile
            if (!dto.getSkillId().equals(candidateSkill.getSkill().getId())) {
                boolean exists = repository.existsByProfileIdAndSkillId(
                        candidateSkill.getProfile().getProfileId(), dto.getSkillId());
                if (exists) {
                    throw new IllegalArgumentException("Kỹ năng này đã tồn tại trong hồ sơ");
                }
            }

            Skill skill = skillRepository.findById(dto.getSkillId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Không tìm thấy kỹ năng với ID: " + dto.getSkillId()));
            candidateSkill.setSkill(skill);
        }

        // Cập nhật level
        if (dto.getLevelId() != null) {
            Level level = levelRepository.findById(dto.getLevelId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy mức độ kỹ năng với ID: " + dto.getLevelId()));
            candidateSkill.setLevel(level);
        } else {
            candidateSkill.setLevel(null);
        }

        // Lưu
        CandidateSkill updatedSkill = repository.save(candidateSkill);

        // Tạo response
        CandidateSkillResponse response = mapper.toResponseDTO(updatedSkill);
        setCategoryInfo(response, updatedSkill.getSkill());
        return response;
    }

    @Override
    @Transactional
    public void delete(Long userId, Long candidateSkillId) {
        // Tìm CandidateSkill by ID
        CandidateSkill candidateSkill = repository.findById(candidateSkillId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy CandidateSkill với ID: " + candidateSkillId));

        // Kiểm tra quyền sở hữu
        profileDomainService.checkOwnership(candidateSkill.getProfile().getUser().getId(), userId);

        // Xóa
        repository.delete(candidateSkill);
    }

    private void setCategoryInfo(CandidateSkillResponse response, Skill skill) {
        Set<Category> categories = skill.getCategories();
        if (!categories.isEmpty()) {
            Category category = categories.iterator().next();
            // Duyệt lên danh mục cha nếu có
            while (category.getParent() != null) {
                category = category.getParent();
            }
            response.setCategoryId(category.getId());
            response.setCategoryName(category.getName());
        }
    }
}