package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("${backend.prefix}/applications")
@RequiredArgsConstructor
public class CvDownloadController {
    private final ApplicationRepository applicationRepo;
    private final WebClient webClient = WebClient.builder().build();

    @GetMapping("/{id}/cv/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        var app = applicationRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        String url = app.getCv();
        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not uploaded");
        }

        RestTemplate restTemplate = new RestTemplate();
        byte[] fileBytes = restTemplate.getForObject(url, byte[].class);

        String fileName = "cv-" + id + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(fileBytes);
    }
}
