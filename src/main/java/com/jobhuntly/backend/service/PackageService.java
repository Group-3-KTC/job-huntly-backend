package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.PackageRequest;
import com.jobhuntly.backend.dto.response.PackageResponse;

import java.util.List;

public interface PackageService {
    List<PackageResponse> getAll();
    PackageResponse getById(Long id);
    PackageResponse create(PackageRequest req);
    PackageResponse update(Long id, PackageRequest req);
    void delete(Long id);
}
