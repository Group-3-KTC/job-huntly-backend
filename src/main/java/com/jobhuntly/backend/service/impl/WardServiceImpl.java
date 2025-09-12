package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.response.WardResponse;
import com.jobhuntly.backend.repository.WardRepository;
import com.jobhuntly.backend.service.WardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WardServiceImpl implements WardService {
    private final WardRepository wardRepository;
    @Override
    public List<WardResponse> getWardByCityName(String cityName) {
        List<WardResponse> wards = wardRepository.findByCity_NameIgnoreCase(cityName)
                .stream()
                .map(w -> new WardResponse(w.getId(), w.getName())) // record dùng constructor
                .toList();

        if (wards.isEmpty()) {
            throw new RuntimeException("Không tìm thấy phường/xã nào thuộc thành phố: " + cityName);
        }
        return wards;
    }
}
