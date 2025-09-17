package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.PackageRequest;
import com.jobhuntly.backend.dto.response.PackageResponse;
import com.jobhuntly.backend.entity.PackageEntity;
import com.jobhuntly.backend.mapper.PackageMapper;
import com.jobhuntly.backend.repository.PackageRepository;
import com.jobhuntly.backend.service.PackageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import static com.jobhuntly.backend.constant.CacheConstant.DICT_PACKAGES;

@Service
@RequiredArgsConstructor
@Transactional
public class PackageServiceImpl implements PackageService {

    private final PackageRepository packageRepository;
    private final PackageMapper packageMapper;

    @Override
    @Cacheable(cacheNames = DICT_PACKAGES, key = "'all'", sync = true)
    public List<PackageResponse> getAll() {
        return packageRepository.findAll()
                .stream()
                .map(packageMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PackageResponse getById(Long id) {
        PackageEntity entity = findByIdOrThrow(id);
        return packageMapper.toResponse(entity);
    }

    @Override
    public PackageResponse create(PackageRequest req) {
        // validate unique code
        if (req.getCode() == null || req.getCode().isBlank()) {
            throw new IllegalArgumentException("code is required");
        }
        if (packageRepository.existsByCodeIgnoreCase(req.getCode())) {
            throw new IllegalStateException("Package code already exists: " + req.getCode());
        }

        PackageEntity entity = packageMapper.toEntity(req);
        PackageEntity saved  = packageRepository.save(entity);
        return packageMapper.toResponse(saved);
    }

    @Override
    public PackageResponse update(Long id, PackageRequest req) {
        PackageEntity entity = findByIdOrThrow(id);

        // validate unique code if changed / provided
        if (req.getCode() != null && !req.getCode().isBlank()) {
            if (packageRepository.existsByCodeIgnoreCaseAndPackageIdNot(req.getCode(), id)) {
                throw new IllegalStateException("Package code already exists: " + req.getCode());
            }
        }

        // patch fields (null sẽ bị bỏ qua nhờ mapper)
        packageMapper.updateEntity(entity, req);

        PackageEntity saved = packageRepository.save(entity);
        return packageMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        PackageEntity entity = findByIdOrThrow(id);
        packageRepository.delete(entity);
    }

    private PackageEntity findByIdOrThrow(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Package not found with id=" + id));
    }

}
