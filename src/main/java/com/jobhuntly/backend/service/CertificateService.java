package com.jobhuntly.backend.service;

import java.util.List;

import com.jobhuntly.backend.dto.request.CertificateRequest;
import com.jobhuntly.backend.dto.response.CertificateResponse;

public interface CertificateService {
    CertificateResponse create(Long userId, CertificateRequest dto);

    List<CertificateResponse> getAll(Long userId);

    CertificateResponse update(Long userId, Long id, CertificateRequest dto);

    void delete(Long userId, Long id);
}
