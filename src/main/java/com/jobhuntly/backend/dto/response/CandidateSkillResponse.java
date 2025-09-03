package com.jobhuntly.backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidateSkillResponse {
    private Long id;
    private Long skillId;
    private String skillName;
    private Long levelId;
    private String levelName;
    private Long categoryId;
    private String categoryName;
    private Long parentCategoryId; 
    private String parentCategoryName;
}