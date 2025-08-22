package com.jobhuntly.backend.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidateSkillRequest {
    private Long skillId;
    private Long levelId;
}