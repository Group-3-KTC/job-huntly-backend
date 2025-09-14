package com.jobhuntly.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class JobPatchRequest {
    @JsonProperty("company_id")
    private Long companyId;

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

    @JsonProperty("salary_min")
    private Long salaryMin;

    @JsonProperty("salary_max")
    private Long salaryMax;

    @JsonProperty("salary_type")
    private Integer salaryType;

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