package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.CityRequest;
import com.jobhuntly.backend.mapper.CityMapper;
import com.jobhuntly.backend.repository.CityRepository;
import com.jobhuntly.backend.service.CityService;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.jobhuntly.backend.constant.CacheConstant.DICT_LOCATIONS_CITY;

@Service
@RequiredArgsConstructor
@Transactional
public class CityServiceImpl implements CityService {
    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    @Override
    @Cacheable(cacheNames = DICT_LOCATIONS_CITY, key = "'all'", sync = true)
    public List<CityRequest> getAllCity() {
        return cityRepository.findAll().stream()
                .map(cityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CityRequest> getCityByName(String namePart) {
        List<CityRequest> result = cityRepository.findByNameContainingIgnoreCase(namePart)
                .stream()
                .map(cityMapper::toDTO)
                .collect(Collectors.toList());
        if (result.isEmpty()) {
            throw new RuntimeException("City not found: " + namePart);
        }
        return result;
    }
}
