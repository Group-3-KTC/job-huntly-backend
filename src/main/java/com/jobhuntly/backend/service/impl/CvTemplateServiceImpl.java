package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.response.CvTemplateResponse;
import com.jobhuntly.backend.dto.response.ProfileCombinedResponse;
import com.jobhuntly.backend.entity.CvTemplate;
import com.jobhuntly.backend.mapper.CvTemplateMapper;
import com.jobhuntly.backend.repository.CvTemplateRepository;
import com.jobhuntly.backend.service.CvTemplateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CvTemplateServiceImpl implements CvTemplateService {

    private final CvTemplateRepository repository;
    private final CloudinaryService cloudinaryService;
    private final TemplateEngine templateEngine;

    @Override
    public CvTemplateResponse create(String name, MultipartFile htmlFile, MultipartFile previewImage)
            throws IOException {
        if (htmlFile == null || htmlFile.isEmpty()) {
            throw new IllegalArgumentException("HTML file is required");
        }
        if (previewImage == null || previewImage.isEmpty()) {
            throw new IllegalArgumentException("Preview image is required");
        }

        CvTemplate entity = CvTemplate.builder()
                .name(name)
                .htmlUrl("PENDING")
                .previewImageUrl("PENDING")
                .build();
        repository.save(entity);

        Long cvId = entity.getId();

        CloudinaryService.CloudAsset htmlAsset = cloudinaryService.uploadCvTemplateFile(cvId, htmlFile, "html");
        CloudinaryService.CloudAsset previewAsset = cloudinaryService.uploadCvTemplateFile(cvId, previewImage,
                "preview");

        entity.setHtmlUrl(htmlAsset.secureUrl());
        entity.setPreviewImageUrl(previewAsset.secureUrl());

        repository.save(entity);
        return CvTemplateMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public List<CvTemplateResponse> getAll() {
        return repository.findAll().stream()
                .map(CvTemplateMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CvTemplateResponse getById(Long id) {
        CvTemplate e = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Template not found: " + id));
        return CvTemplateMapper.toResponse(e);
    }

    @Override
    public CvTemplateResponse update(Long id, String name, MultipartFile htmlFile, MultipartFile previewImage)
            throws IOException {
        CvTemplate e = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Template not found: " + id));

        if (name != null && !name.isBlank()) {
            e.setName(name);
        }
        if (htmlFile != null && !htmlFile.isEmpty()) {
            CloudinaryService.CloudAsset htmlAsset = cloudinaryService.uploadCvTemplateFile(id, htmlFile, "html");
            e.setHtmlUrl(htmlAsset.secureUrl());
        }
        if (previewImage != null && !previewImage.isEmpty()) {
            CloudinaryService.CloudAsset previewAsset = cloudinaryService.uploadCvTemplateFile(id, previewImage,
                    "preview");
            e.setPreviewImageUrl(previewAsset.secureUrl());
        }

        repository.save(e);
        return CvTemplateMapper.toResponse(e);
    }
    
    @Override
    public void delete(Long id) {
        CvTemplate template = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Template not found: " + id));
        try {
            if (template.getHtmlUrl() != null) {
                cloudinaryService.deleteCvTemplateFileByUrl(template.getHtmlUrl());
            }
            if (template.getPreviewImageUrl() != null) {
                cloudinaryService.deleteCvTemplateFileByUrl(template.getPreviewImageUrl());
            }
        } catch (IOException ignored) {
        }

        repository.delete(template);
    }

    public String renderHtml(Long templateId, ProfileCombinedResponse profile) {
        String htmlTemplate = fetchHtmlFromCloudinary(templateId);

        // Create a StringTemplateResolver to handle raw HTML strings
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);

        // Use a new TemplateEngine with the StringTemplateResolver
        TemplateEngine stringTemplateEngine = new TemplateEngine();
        stringTemplateEngine.setTemplateResolver(resolver);

        Context ctx = new Context();
        ctx.setVariable("profile", profile);

        // Set variables for Thymeleaf context (consistent with JSON structure)
        if (profile.getEducation() != null && !profile.getEducation().isEmpty()) {
            ctx.setVariable("education", profile.getEducation());
        }
        if (profile.getWorkExperience() != null && !profile.getWorkExperience().isEmpty()) {
            ctx.setVariable("workExperience", profile.getWorkExperience());
        }
        if (profile.getCertificates() != null && !profile.getCertificates().isEmpty()) {
            ctx.setVariable("certificates", profile.getCertificates());
        }
        if (profile.getAwards() != null && !profile.getAwards().isEmpty()) {
            ctx.setVariable("awards", profile.getAwards());
        }
        if (profile.getSoftSkills() != null && !profile.getSoftSkills().isEmpty()) {
            ctx.setVariable("softSkills", profile.getSoftSkills());
        }
        if (profile.getCandidateSkills() != null && !profile.getCandidateSkills().isEmpty()) {
            ctx.setVariable("candidateSkills", profile.getCandidateSkills());
        }

        // Process the HTML string directly
        return stringTemplateEngine.process(htmlTemplate, ctx);
    }

    public byte[] renderPdf(Long templateId, ProfileCombinedResponse profile) {
        String htmlContent = renderHtml(templateId, profile);

        try {
            File tempHtml = File.createTempFile("cv_", ".html");
            Files.writeString(tempHtml.toPath(), htmlContent, StandardCharsets.UTF_8);

            File tempPdf = File.createTempFile("cv_", ".pdf");

            // call wkhtmltopdf options
            ProcessBuilder pb = new ProcessBuilder(
                    "wkhtmltopdf",
                    "--page-size", "A4",
                    "--orientation", "Portrait",
                    "--margin-top", "0mm",
                    "--margin-bottom", "0mm",
                    "--margin-left", "0mm",
                    "--margin-right", "0mm",
                    "--print-media-type",
                    "--enable-local-file-access",
                    "--encoding", "utf-8",
                    "--dpi", "300",
                    tempHtml.getAbsolutePath(),
                    tempPdf.getAbsolutePath()
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("wkhtmltopdf failed, exit code " + exitCode);
            }

            byte[] pdfBytes = Files.readAllBytes(tempPdf.toPath());

            tempHtml.delete();
            tempPdf.delete();

            return pdfBytes;
        } catch (Exception e) {
            throw new RuntimeException("Failed to render PDF with wkhtmltopdf", e);
        }
    }

    private String fetchHtmlFromCloudinary(Long templateId) {
        CvTemplate template = repository.findById(templateId)
                .orElseThrow(() -> new NoSuchElementException("Template not found: " + templateId));
        try {
            URL url = new URL(template.getHtmlUrl());
            return new String(url.openStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch HTML template from Cloudinary", e);
        }
    }
}
