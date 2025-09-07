package com.jobhuntly.backend.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.jobhuntly.backend.dto.response.CvTemplateResponse;
import com.jobhuntly.backend.dto.response.ProfileCombinedResponse; 

public interface CvTemplateService {
    CvTemplateResponse create(String name, MultipartFile htmlFile, MultipartFile previewImage) throws IOException;

    List<CvTemplateResponse> getAll();

    CvTemplateResponse getById(Long id);

    CvTemplateResponse update(Long id, String name, MultipartFile htmlFile, MultipartFile previewImage)
            throws IOException;

    void delete(Long id);

    String renderHtml(Long templateId, ProfileCombinedResponse profile);

    byte[] renderPdf(Long templateId, ProfileCombinedResponse profile);
}
