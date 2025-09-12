package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.response.WorkTypeResponse;
import com.jobhuntly.backend.mapper.WorkTypeMapper;
import com.jobhuntly.backend.repository.WorkTypeRepository;
import com.jobhuntly.backend.service.WorkTypeService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.jobhuntly.backend.constant.CacheConstant.DICT_WORK_TYPES;

@Service
@AllArgsConstructor
@Transactional
public class WorkTypeServiceImpl implements WorkTypeService {
    private final WorkTypeMapper workTypeMapper;
    private final WorkTypeRepository workTypeRepository;

    @Override
    @Cacheable(cacheNames = DICT_WORK_TYPES, key = "'all'", sync = true)
    public List<WorkTypeResponse> getAllWorkType() {
        return workTypeRepository.findAll().stream()
                .map(workTypeMapper::toResponse)
                .collect(Collectors.toList());
    }
}
