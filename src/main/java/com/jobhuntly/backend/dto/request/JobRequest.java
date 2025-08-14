package com.jobhuntly.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequest {
    @NotNull
    @JsonProperty("company_id")
    private Long companyId;

    @NotBlank
    private String title;

    @JsonProperty("date_post")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate datePost;

    @JsonProperty("expired_date")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate expiredDate;

    private String description;
    private String requirements;
    private String benefits;
    private String location;
    private String status;

    // --- lương (VND) ---
    @JsonProperty("salary_min")
    private Long salaryMin;

    @JsonProperty("salary_max")
    private Long salaryMax;

    @JsonProperty("salary_type")
    private Integer salaryType;       // 0=RANGE, 1=NEGOTIABLE, 2=HIDDEN

    @JsonProperty("category_names")
    private List<String> categoryNames;

    @JsonProperty("skill_names")
    private List<String> skillNames;

    @JsonProperty("level_names")
    private List<String> levelNames;

    @JsonProperty("work_type_names")
    private List<String> workTypeNames;

    @JsonProperty("ward_ids")
    private List<Long> wardIds;
}
