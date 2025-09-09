package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.SavedJobRequest;
import com.jobhuntly.backend.dto.response.SavedJobResponse;
import com.jobhuntly.backend.entity.Company;
import com.jobhuntly.backend.entity.Job;
import com.jobhuntly.backend.entity.SavedJob;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SavedJobMapper {
    SavedJob toEntity(SavedJobRequest savedJobRequest);

    @Mapping(target = "jobId",         source = "job.id")
    @Mapping(target = "createdAt",     source = "saved.createdAt")
    @Mapping(target = "companyName",   source = "company.companyName")
    @Mapping(target = "companyAvatar", source = "company.avatar")
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "titleJob",      source = "job.title")
    @Mapping(target = "skillJob",      source = "skillNames")
    @Mapping(target = "salaryDisplay", ignore = true)
    SavedJobResponse toResponse(SavedJob saved, Job job, Company company, List<String> skillNames);

    @AfterMapping
    default void fillSalaryDisplay(
            SavedJob saved,
            Job job,
            Company company,
            List<String> skillNames,
            @MappingTarget SavedJobResponse.SavedJobResponseBuilder target
    ) {
        if (job == null) return;

        Integer t = job.getSalaryType(); // 0=range, 1=negotiable, 2=hidden (ví dụ)
        Long min = job.getSalaryMin();
        Long max = job.getSalaryMax();

        if (t == null) {
            if (min != null && max != null) {
                target.salaryDisplay(String.format("%,d - %,d VND", min, max));
            } else {
                target.salaryDisplay("Thỏa thuận");
            }
            return;
        }

        switch (t) {
            case 0 -> { // range
                if (min != null && max != null) {
                    target.salaryDisplay(String.format("%,d - %,d VND", min, max));
                } else if (min != null) {
                    target.salaryDisplay(String.format("Từ %,d VND", min));
                } else if (max != null) {
                    target.salaryDisplay(String.format("Đến %,d VND", max));
                } else {
                    target.salaryDisplay("Thỏa thuận");
                }
            }
            case 1 -> target.salaryDisplay("Thỏa thuận");
            case 2 -> target.salaryDisplay("Ẩn lương");
            default -> {
                if (min != null && max != null) {
                    target.salaryDisplay(String.format("%,d - %,d VND", min, max));
                } else {
                    target.salaryDisplay("Thỏa thuận");
                }
            }
        }
    }
}
