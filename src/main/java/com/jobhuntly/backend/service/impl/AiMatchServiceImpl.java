package com.jobhuntly.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobhuntly.backend.dto.ai.MatchResponse;
import com.jobhuntly.backend.dto.response.JobResponse;
import com.jobhuntly.backend.dto.response.ProfileCombinedResponse;
import com.jobhuntly.backend.entity.Application;
import com.jobhuntly.backend.repository.ApplicationRepository;
import com.jobhuntly.backend.service.AiMatchService;
import com.jobhuntly.backend.service.JobService;
import com.jobhuntly.backend.service.ProfileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;
import java.util.Base64;
import java.util.stream.Collectors;

@Service
public class AiMatchServiceImpl implements AiMatchService {

    private final JobService jobService;
    private final ProfileService profileService;
    private final ApplicationRepository applicationRepository;
    private final RestTemplate rt = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.apiKey}")
    private String apiKey;
    @Value("${gemini.model}")
    private String model;
    @Value("${gemini.endpoint}")
    private String endpoint; // ví dụ: https://generativelanguage.googleapis.com/v1beta/models/

    public AiMatchServiceImpl(JobService jobService, ProfileService profileService, ApplicationRepository applicationRepository) {
        this.jobService = jobService;
        this.profileService = profileService;
        this.applicationRepository = applicationRepository;
    }

    @Override
    public MatchResponse matchCandidateToJob(Long userId, Long jobId, String resumeFileId, String resumeText, boolean useFileApi) {
        // 1) Lấy JD text
        String jd = buildJobDescription(jobId);

        // 2) Quyết định nguồn CV
        byte[] resumePdfBytes = loadResumePdfBytes(userId, jobId, resumeFileId);
        if (resumePdfBytes == null && (resumeText == null || resumeText.isBlank())) {
            ProfileCombinedResponse p = profileService.getCombinedProfile(userId);
            resumeText = buildResumeTextFromProfile(p);
        }

        // 3) Gọi Gemini
        try {
            if (resumePdfBytes != null && !useFileApi) {
                return callGeminiInlinePdf(resumePdfBytes, jd);
            }
            if (resumePdfBytes != null && useFileApi) {
                return callGeminiWithFileApi(resumePdfBytes, "application/pdf", "cv.pdf", jd);
            }
            return callGeminiWithText(resumeText, jd);
        } catch (HttpClientErrorException e) {
            if (resumePdfBytes != null && e.getStatusCode().value() == 413) {
                return callGeminiWithFileApi(resumePdfBytes, "application/pdf", "cv.pdf", jd);
            }
            return new MatchResponse(0, List.of("AI lỗi: " + e.getStatusCode()));
        } catch (Exception ex) {
            return new MatchResponse(0, List.of("Lỗi nội bộ khi gọi AI"));
        }
    }

    // MỚI: chấm với file vừa upload
    @Override
    public MatchResponse matchByUploadedFile(Long userId, Long jobId, byte[] pdfBytes, boolean useFileApi) {
        String jd = buildJobDescription(jobId);
        try {
            if (!useFileApi) {
                return callGeminiInlinePdf(pdfBytes, jd);
            }
            return callGeminiWithFileApi(pdfBytes, "application/pdf", "cv.pdf", jd);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 413) {
                return callGeminiWithFileApi(pdfBytes, "application/pdf", "cv.pdf", jd);
            }
            return new MatchResponse(0, List.of("AI lỗi: " + e.getStatusCode()));
        } catch (Exception ex) {
            return new MatchResponse(0, List.of("Lỗi nội bộ khi gọi AI"));
        }
    }

    private MatchResponse callGeminiInlinePdf(byte[] pdf, String jd) {
        String base = normalizeEndpoint(endpoint); // .../v1beta/models
        String url = base + "/" + model + ":generateContent?key=" + apiKey;
        String b64 = Base64.getEncoder().encodeToString(pdf);
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(
                                Map.of("inline_data", Map.of("mime_type", "application/pdf", "data", b64)),
                                Map.of("text", buildPrompt(jd))
                        )
                ))
        );
        return postAndParse(url, body);
    }

    private MatchResponse callGeminiWithText(String resume, String jd) {
        String base = normalizeEndpoint(endpoint);
        String url = base + "/" + model + ":generateContent?key=" + apiKey;
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(
                                Map.of("text", "CV:\n" + safeTrim(resume)),
                                Map.of("text", buildPrompt(jd))
                        )
                ))
        );
        return postAndParse(url, body);
    }

    private MatchResponse callGeminiWithFileApi(byte[] bytes, String mime, String filename, String jd) {
        // 1) Upload
        String root = extractApiRoot(endpoint); // https://generativelanguage.googleapis.com
        String uploadUrl = root + "/upload/v1beta/files?key=" + apiKey;
        HttpHeaders upHeaders = new HttpHeaders();
        upHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        org.springframework.util.LinkedMultiValueMap<String, Object> form = new org.springframework.util.LinkedMultiValueMap<>();
        form.add("file", new org.springframework.core.io.ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        });
        ResponseEntity<Map> upResp = rt.postForEntity(uploadUrl, new HttpEntity<>(form, upHeaders), Map.class);
        Map up = upResp.getBody();
        String fileUri = null;
        if (up != null) {
            Object directUri = up.get("uri");
            if (directUri != null) {
                fileUri = directUri.toString();
            } else {
                Object fileObj = up.get("file");
                if (fileObj instanceof Map<?, ?> fileMap) {
                    Object nestedUri = fileMap.get("uri");
                    if (nestedUri != null) fileUri = nestedUri.toString();
                }
            }
        }

        // 2) Generate
        String base = normalizeEndpoint(endpoint);
        String genUrl = base + "/" + model + ":generateContent?key=" + apiKey;
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(
                                Map.of("file_data", Map.of("file_uri", fileUri, "mime_type", mime)),
                                Map.of("text", buildPrompt(jd))
                        )
                ))
        );
        return postAndParse(genUrl, body);
    }

    private MatchResponse postAndParse(String url, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> resp = rt.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            return new MatchResponse(0, List.of("AI không phản hồi"));
        }
        return parseGeminiResponse(resp.getBody());
    }

    private String buildPrompt(String jd) {
        return """
                Bạn là hệ thống ATS. So sánh CV và JD sau. 
                Chỉ trả về JSON hợp lệ duy nhất: {"score": <int 0..100>, "reasons": ["...","..."]}
                
                JD:
                %s
                """.formatted(safeTrim(jd));
    }

    private MatchResponse parseGeminiResponse(String body) {
        try {
            JsonNode root = mapper.readTree(body);
            // Lấy toàn bộ text từ tất cả parts (nếu có)
            JsonNode partsNode = root.path("candidates").path(0).path("content").path("parts");
            StringBuilder sb = new StringBuilder();
            if (partsNode.isArray()) {
                for (JsonNode p : partsNode) {
                    String t = p.path("text").asText(null);
                    if (t != null) {
                        if (sb.length() > 0) sb.append('\n');
                        sb.append(t);
                    }
                }
            }
            String raw = sb.length() > 0 ? sb.toString() : root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");

            String cleaned = stripCodeFences(raw).trim();
            String jsonCandidate = extractJsonObject(cleaned);
            if (jsonCandidate == null || jsonCandidate.isBlank()) {
                return new MatchResponse(0, List.of("Không parse được phản hồi AI"));
            }
            JsonNode json = mapper.readTree(jsonCandidate);
            int score = Math.max(0, Math.min(100, json.path("score").asInt(0)));
            List<String> reasons = new ArrayList<>();
            if (json.path("reasons").isArray()) json.path("reasons").forEach(n -> reasons.add(n.asText()));
            return new MatchResponse(score, reasons.isEmpty() ? List.of("Không có lý do cụ thể.") : reasons);
        } catch (Exception e) {
            return new MatchResponse(0, List.of("Không parse được phản hồi AI"));
        }
    }

    private String stripCodeFences(String s) {
        if (s == null) return "";
        String trimmed = s.trim();
        if (trimmed.startsWith("```")) {
            // loại bỏ dòng mở ```... và dòng đóng ```
            trimmed = trimmed.replaceAll("^```[a-zA-Z0-9_-]*\\n", "");
            int idx = trimmed.lastIndexOf("```");
            if (idx >= 0) trimmed = trimmed.substring(0, idx);
        }
        return trimmed.trim();
    }

    private String extractJsonObject(String s) {
        if (s == null) return null;
        int start = s.indexOf('{');
        if (start < 0) return null;
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return s.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    private String buildJobDescription(Long jobId) {
        JobResponse job = jobService.getById(jobId);
        if (job == null) return "";
        String skills = safeJoin(job.getSkillNames());
        String levels = safeJoin(job.getLevelNames());
        String workTypes = safeJoin(job.getWorkTypeNames());
        String categories = safeJoin(job.getCategoryNames());
        String wards = job.getWards() != null ? job.getWards().stream().map(JobResponse.WardBrief::getName).collect(Collectors.joining(", ")) : "";
        StringBuilder sb = new StringBuilder();
        if (job.getTitle() != null) sb.append("Title: ").append(job.getTitle()).append("\n");
        if (levels != null && !levels.isEmpty()) sb.append("Level: ").append(levels).append("\n");
        if (skills != null && !skills.isEmpty()) sb.append("Skills: ").append(skills).append("\n");
        if (categories != null && !categories.isEmpty()) sb.append("Categories: ").append(categories).append("\n");
        if (workTypes != null && !workTypes.isEmpty()) sb.append("Work types: ").append(workTypes).append("\n");
        if (job.getLocation() != null) sb.append("Location: ").append(job.getLocation()).append("\n");
        if (!wards.isEmpty()) sb.append("Wards: ").append(wards).append("\n");
        if (job.getSalaryDisplay() != null) sb.append("Salary: ").append(job.getSalaryDisplay()).append("\n");
        if (job.getDescription() != null) sb.append("Description:\n").append(job.getDescription()).append("\n");
        if (job.getRequirements() != null) sb.append("Requirements:\n").append(job.getRequirements()).append("\n");
        if (job.getBenefits() != null) sb.append("Benefits:\n").append(job.getBenefits()).append("\n");
        return sb.toString();
    }

    private byte[] loadResumePdfBytes(Long userId, Long jobId, String resumeFileId) {
        // Ưu tiên: nếu ứng viên đã nộp cho job này -> lấy URL CV từ application
        Optional<Application> appOpt = applicationRepository.findByUser_IdAndJob_Id(userId, jobId);
        if (appOpt.isPresent()) {
            String url = appOpt.get().getCv();
            byte[] bytes = fetchBytes(url);
            if (bytes != null && bytes.length > 0) return bytes;
        }
        // Có thể mở rộng: nếu resumeFileId mang ý nghĩa applicationId cụ thể
        try {
            if (resumeFileId != null && !resumeFileId.isBlank()) {
                Long appId = Long.valueOf(resumeFileId);
                Optional<Application> byId = applicationRepository.findById(appId);
                if (byId.isPresent() && Objects.equals(byId.get().getUser().getId(), userId)) {
                    byte[] bytes = fetchBytes(byId.get().getCv());
                    if (bytes != null && bytes.length > 0) return bytes;
                }
            }
        } catch (NumberFormatException ignore) {
        }
        return null;
    }

    private byte[] fetchBytes(String url) {
        if (url == null || url.isBlank()) return null;
        try {
            ResponseEntity<byte[]> res = rt.exchange(URI.create(url), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), byte[].class);
            if (res.getStatusCode().is2xxSuccessful()) return res.getBody();
        } catch (Exception ignore) {
        }
        return null;
    }

    private String buildResumeTextFromProfile(ProfileCombinedResponse p) {
        if (p == null) return "";
        StringBuilder sb = new StringBuilder();
        if (p.getFullName() != null) sb.append("Name: ").append(p.getFullName()).append("\n");
        if (p.getTitle() != null) sb.append("Title: ").append(p.getTitle()).append("\n");
        if (p.getAboutMe() != null) sb.append("About: ").append(p.getAboutMe()).append("\n");
        if (p.getEmail() != null) sb.append("Email: ").append(p.getEmail()).append("\n");
        if (p.getPhone() != null) sb.append("Phone: ").append(p.getPhone()).append("\n");
        if (p.getGender() != null) sb.append("Gender: ").append(p.getGender()).append("\n");
        if (p.getDateOfBirth() != null) sb.append("Date of birth: ").append(p.getDateOfBirth()).append("\n");
        if (p.getPersonalLink() != null && !p.getPersonalLink().isBlank()) sb.append("Personal link: ").append(p.getPersonalLink()).append("\n");

        if (p.getCandidateSkills() != null && !p.getCandidateSkills().isEmpty()) {
            sb.append("Skills: \n");
            p.getCandidateSkills().forEach(s -> {
                String name = s.getSkillName() != null ? s.getSkillName() : String.valueOf(s.getId());
                String level = s.getLevelName() != null ? s.getLevelName() : "";
                String category = s.getCategoryName() != null ? s.getCategoryName() : "";
                String parentCategory = s.getParentCategoryName() != null ? s.getParentCategoryName() : "";
                sb.append("- ").append(name);
                if (!level.isEmpty()) sb.append(" (level: ").append(level).append(")");
                if (!category.isEmpty()) sb.append(", category: ").append(category);
                if (!parentCategory.isEmpty()) sb.append(", parentCategory: ").append(parentCategory);
                sb.append("\n");
            });
        }
        if (p.getSoftSkills() != null && !p.getSoftSkills().isEmpty()) {
            sb.append("Soft skills: \n");
            p.getSoftSkills().forEach(s -> {
                String name = s.getName() != null ? s.getName() : String.valueOf(s.getId());
                String level = s.getLevel() != null ? s.getLevel() : "";
                String desc = s.getDescription() != null ? s.getDescription() : "";
                sb.append("- ").append(name);
                if (!level.isEmpty()) sb.append(" (level: ").append(level).append(")");
                if (!desc.isEmpty()) sb.append(", note: ").append(desc);
                sb.append("\n");
            });
        }
        if (p.getWorkExperience() != null) {
            sb.append("Experience:\n");
            p.getWorkExperience().forEach(w -> {
                if (w.getCompanyName() != null) sb.append("- Company: ").append(w.getCompanyName()).append("\n");
                if (w.getPosition() != null) sb.append("  Position: ").append(w.getPosition()).append("\n");
                if (w.getDuration() != null) sb.append("  Duration: ").append(w.getDuration()).append("\n");
                if (w.getDescription() != null) sb.append("  Desc: ").append(w.getDescription()).append("\n");
            });
        }
        if (p.getEducation() != null) {
            sb.append("Education:\n");
            p.getEducation().forEach(e -> {
                if (e.getSchoolName() != null) sb.append("- School: ").append(e.getSchoolName()).append("\n");
                if (e.getMajors() != null) sb.append("  Major: ").append(e.getMajors()).append("\n");
                if (e.getDegree() != null) sb.append("  Degree: ").append(e.getDegree()).append("\n");
                if (e.getDuration() != null) sb.append("  Duration: ").append(e.getDuration()).append("\n");
            });
        }
        if (p.getCertificates() != null && !p.getCertificates().isEmpty()) {
            sb.append("Certificates: \n");
            p.getCertificates().forEach(c -> {
                String name = c.getCerName() != null ? c.getCerName() : "";
                if (!name.isEmpty()) sb.append("- ").append(name);
                if (c.getIssuer() != null) sb.append(" (issuer: ").append(c.getIssuer()).append(")");
                if (c.getDate() != null) sb.append(", date: ").append(c.getDate());
                if (c.getDescription() != null) sb.append(", desc: ").append(c.getDescription());
                sb.append("\n");
            });
        }
        if (p.getAwards() != null && !p.getAwards().isEmpty()) {
            sb.append("Awards: \n");
            p.getAwards().forEach(a -> {
                String name = a.getName() != null ? a.getName() : "";
                if (!name.isEmpty()) sb.append("- ").append(name);
                if (a.getIssuer() != null) sb.append(" (issuer: ").append(a.getIssuer()).append(")");
                if (a.getDate() != null) sb.append(", date: ").append(a.getDate());
                if (a.getDescription() != null) sb.append(", desc: ").append(a.getDescription());
                sb.append("\n");
            });
        }
        return sb.toString();
    }

    private String normalizeEndpoint(String ep) {
        if (ep == null) return "https://generativelanguage.googleapis.com/v1beta/models";
        String s = ep.endsWith("/") ? ep.substring(0, ep.length() - 1) : ep;
        // đảm bảo kết thúc bằng /v1beta/models
        if (s.endsWith("/models")) return s;
        if (s.endsWith("/models/")) return s.substring(0, s.length() - 1);
        return s;
    }

    private String extractApiRoot(String ep) {
        // từ https://generativelanguage.googleapis.com/v1beta/models -> https://generativelanguage.googleapis.com
        if (ep == null) return "https://generativelanguage.googleapis.com";
        int idx = ep.indexOf("/v1beta/");
        if (idx > 0) return ep.substring(0, idx);
        return "https://generativelanguage.googleapis.com";
    }

    private String safeJoin(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return list.stream().filter(Objects::nonNull).collect(Collectors.joining(", "));
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}