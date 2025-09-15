package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.response.FollowResponse;
import com.jobhuntly.backend.entity.Company;
import com.jobhuntly.backend.entity.Follow;
import com.jobhuntly.backend.mapper.FollowMapper;
import com.jobhuntly.backend.repository.CompanyRepository;
import com.jobhuntly.backend.repository.FollowRepository;
import com.jobhuntly.backend.service.FollowService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.jobhuntly.backend.mapper.CvTemplateMapper.toResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final CompanyRepository companyRepository;
    private final FollowMapper followMapper;

    @Override
    public void create(Long userId, Long companyId) {
        // Kiểm tra tồn tại, nếu có rồi thì bỏ qua
        boolean exists = followRepository.existsByUserIdAndCompanyId(userId, companyId);
        if (exists) {
            return; // idempotent
        }

        Follow toSave = new Follow();
        toSave.setUserId(userId);
        toSave.setCompanyId(companyId);
        followRepository.save(toSave);
    }

    @Override
    public void delete(Long userId, Long companyId) {
        followRepository.deleteByUserIdAndCompanyId(userId, companyId);
    }

    @Override
    public long countFollowers(Long companyId) {
        return followRepository.countByCompanyId(companyId);
    }

    @Override
    public Page<FollowResponse> getFollowedCompanies(Long userId, Pageable pageable) {
        Page<Follow> page = followRepository.findByUserId(userId, pageable);

        if (page.isEmpty()) {
            return page.map(item -> null);
        }

        List<Long> companyIds = page.getContent().stream()
                .map(Follow::getCompanyId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Company> companyMap = companyRepository.findByIdIn(companyIds).stream()
                .collect(Collectors.toMap(Company::getId, Function.identity()));

        return page.map(f -> {
            Company company = companyMap.get(f.getCompanyId());
            if (company == null) {
                return new FollowResponse(
                        f.getFollowId(),
                        f.getUserId(),
                        f.getCompanyId(),
                        null,
                        null,
                        0L,
                        null
                );
            }
            return followMapper.toResponse(f, company);
        });

    }

    @Override
    public boolean isFollowed(Long userId, Long companyId) {
        return followRepository.existsByUserIdAndCompanyId(userId, companyId);
    }
}
