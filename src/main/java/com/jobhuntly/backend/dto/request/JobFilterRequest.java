package com.jobhuntly.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobFilterRequest {
    private String keyword;
    private String companyName;

    private String cityName;

    private Set<String> categoryNames;
    private Set<String> skillNames;
    private Set<String> levelNames;
    private Set<String> workTypeNames;
    private Set<String> wardNames;

    private boolean matchAllCategories = false;
    private boolean matchAllSkills = false;
    private boolean matchAllLevels = false;
    private boolean matchAllWorkTypes = false;
    private boolean matchAllWards = false;

    private Long salaryMin;
    private Long salaryMax;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate postedFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate postedTo;

    private String status;       // ví dụ: ACTIVE, INACTIVE, DRAFT

    // true: chỉ lấy job còn hạn (expiredDate >= today)
    private Boolean onlyActive;
}
