package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.response.LevelResponse;
import com.jobhuntly.backend.mapper.LevelMapper;
import com.jobhuntly.backend.repository.LevelRepository;
import com.jobhuntly.backend.service.LevelService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class LevelServiceImpl implements LevelService {
    private final LevelRepository levelRepository;
    private final LevelMapper levelMapper;

    @Override
    public List<LevelResponse> getAllLevels() {
        return levelRepository.findAll().stream()
                .map(levelMapper::toResponse)
                .collect(Collectors.toList());
    }
}
