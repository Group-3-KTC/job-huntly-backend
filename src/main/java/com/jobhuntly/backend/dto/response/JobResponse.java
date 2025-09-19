package com.jobhuntly.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class JobResponse {
    private Long id;

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
    private Integer salaryType;        // 0=RANGE, 1=NEGOTIABLE, 2=HIDDEN

    private String salaryDisplay;

    @JsonProperty("company")
    private CompanyBrief company;

    @JsonProperty("category_names")
    private List<String> categoryNames;

    @JsonProperty("skill_names")
    private List<String> skillNames;

    @JsonProperty("level_names")
    private List<String> levelNames;

    @JsonProperty("work_type_names")
    private List<String> workTypeNames;

    @JsonProperty("wards")
    private List<WardBrief> wards;

    // ======= Brief DTOs =======

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyBrief {
        @JsonProperty("company_id")
        private Long id;

        @JsonProperty("company_name")
        private String name;

        private String avatar;

        private Boolean isProCompany;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WardBrief {
        @JsonProperty("ward_name")
        private String name;

        @JsonProperty("city_id")
        private Long cityId;
    }
}
