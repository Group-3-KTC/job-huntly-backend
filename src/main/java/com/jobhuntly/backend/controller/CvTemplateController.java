package com.jobhuntly.backend.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.jobhuntly.backend.dto.response.CvTemplateResponse;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.service.CvTemplateService;
import com.jobhuntly.backend.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${backend.prefix}/cv-templates")
@RequiredArgsConstructor
public class CvTemplateController {

    private final CvTemplateService cvTemplateService;
    private final ProfileService profileService;

    // Create: multipart (name + htmlFile + previewImage)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CvTemplateResponse> create(
            @RequestPart("name") String name,
            @RequestPart("htmlFile") MultipartFile htmlFile,
            @RequestPart("previewImage") MultipartFile previewImage) throws IOException {
        return ResponseEntity.ok(cvTemplateService.create(name, htmlFile, previewImage));
    }
    @GetMapping
    public ResponseEntity<List<CvTemplateResponse>> getAll() {
        return ResponseEntity.ok(cvTemplateService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CvTemplateResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cvTemplateService.getById(id));
    }
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CvTemplateResponse> update(
            @PathVariable Long id,
            @RequestPart(required = false, value = "name") String name,
            @RequestPart(required = false, value = "htmlFile") MultipartFile htmlFile,
            @RequestPart(required = false, value = "previewImage") MultipartFile previewImage) throws IOException {
        return ResponseEntity.ok(cvTemplateService.update(id, name, htmlFile, previewImage));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cvTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Preview CV with data injected into template
    @GetMapping("/{templateId}/preview")
    public ResponseEntity<String> previewTemplate(@PathVariable Long templateId) {
        Long userId = SecurityUtils.getCurrentUserId();
        var combinedProfile = profileService.getCombinedProfile(userId);

        String html = cvTemplateService.renderHtml(templateId, combinedProfile);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML) 
                .body(html);
    }

    // Download CV as PDF
    @GetMapping("/download/{templateId}")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable Long templateId) {
        Long userId = SecurityUtils.getCurrentUserId();
        var combinedProfile = profileService.getCombinedProfile(userId);

        byte[] pdfBytes = cvTemplateService.renderPdf(templateId, combinedProfile);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"cv.pdf\"")
                .body(pdfBytes);
    }
}
