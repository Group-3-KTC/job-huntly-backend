package com.jobhuntly.backend.dto.ai;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchRequest {
    private Long jobId;
    private String resumeFileId; // optional: id file CV trong hệ thống bạn
    private String resumeText;   // optional: text CV nếu không dùng file
    private Boolean useFileApi;  // optional: fallback Gemini File API
}