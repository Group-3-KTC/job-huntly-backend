package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.ApplicationRequest;
import com.jobhuntly.backend.dto.response.ApplicationByUserResponse;
import com.jobhuntly.backend.dto.response.ApplicationResponse;
import com.jobhuntly.backend.entity.Application;
import com.jobhuntly.backend.entity.Job;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {
    ApplicationResponse toResponse (Application application);
    Application toEntity (ApplicationRequest applicationRequest);
    List<ApplicationResponse> toListApplication (List<Application> applicationList);

    // ====== Mapper cho getByUser ======
    @Mapping(target = "applicationId", source = "id")
    @Mapping(target = "status",        source = "status")
    @Mapping(target = "cv",            source = "cv")
    @Mapping(target = "createdAt",     source = "createdAt")
    @Mapping(target = "description", source = "description")

    @Mapping(target = "jobId",         source = "job.id")
    @Mapping(target = "title",         source = "job.title")
    @Mapping(target = "expiredDate",   source = "job.expiredDate")

    @Mapping(target = "companyId",     source = "job.company.id")
    @Mapping(target = "companyName",   source = "job.company.companyName")
    @Mapping(target = "companyAvatar", source = "job.company.avatar")
    @Mapping(target = "salaryDisplay", ignore = true)
    ApplicationByUserResponse toByUserResponse(Application application);

    List<ApplicationByUserResponse> toByUserList(List<Application> apps);


    @AfterMapping
    default void fillSalaryDisplay(Application source,
                                   @MappingTarget ApplicationByUserResponse.ApplicationByUserResponseBuilder target) {
        Job job = source.getJob();
        if (job == null) return;

        Integer t = job.getSalaryType(); // 0=range, 1=negotiable, 2=hidden (ví dụ)
        if (t == null) return;

        switch (t) {
            case 0 -> {
                Long min = job.getSalaryMin(), max = job.getSalaryMax();
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

                Long min = job.getSalaryMin(), max = job.getSalaryMax();
                if (min != null && max != null) {
                    target.salaryDisplay(String.format("%,d - %,d VND", min, max));
                } else {
                    target.salaryDisplay("Thỏa thuận");
                }
            }
        }
    }
}
