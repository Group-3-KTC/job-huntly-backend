package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.CityDTO;
import com.jobhuntly.backend.mapper.CityMapper;
import com.jobhuntly.backend.repository.CityRepository;
import com.jobhuntly.backend.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CityServiceImpl implements CityService {
    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    @Override
    public List<CityDTO> getAllCity() {
        return cityRepository.findAll().stream()
                .map(cityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CityDTO> getCityByName(String namePart) {
        List<CityDTO> result = cityRepository.findByNameContainingIgnoreCase(namePart)
                .stream()
                .map(cityMapper::toDTO)
                .collect(Collectors.toList());
        if (result.isEmpty()) {
            throw new RuntimeException("City not found: " + namePart);
        }
        return result;
    }
}
