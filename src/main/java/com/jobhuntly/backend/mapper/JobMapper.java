package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.JobRequest;
import com.jobhuntly.backend.dto.response.JobResponse;
import com.jobhuntly.backend.entity.*;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface JobMapper {


    @Mapping(source = "company",    target = "company")
    @Mapping(source = "categories", target = "categoryNames", qualifiedByName = "toCategoryNames")
    @Mapping(source = "skills",     target = "skillNames",    qualifiedByName = "toSkillNames")
    @Mapping(source = "levels",     target = "levelNames",    qualifiedByName = "toLevelNames")
    @Mapping(source = "workTypes",  target = "workTypeNames", qualifiedByName = "toWorkTypeNames")
    @Mapping(source = "wards",      target = "wards")
    JobResponse toResponse(Job job);


    @Mapping(source = "id",          target = "id")
    @Mapping(source = "companyName", target = "name")
    @Mapping(source = "avatar",      target = "avatar")
    JobResponse.CompanyBrief toCompanyBrief(Company company);


    @Mapping(source = "name",    target = "name")
    @Mapping(source = "city.id", target = "cityId")
    JobResponse.WardBrief toWardBrief(Ward ward);
    List<JobResponse.WardBrief> toWardBriefList(Set<Ward> wards);

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "company",    ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "skills",     ignore = true)
    @Mapping(target = "levels",     ignore = true)
    @Mapping(target = "workTypes",  ignore = true)
    @Mapping(target = "wards",      ignore = true)
    Job toEntity(JobRequest jobRequest);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "id",     target = "id")
    @Mapping(source = "title",  target = "title")
    @Mapping(source = "company", target = "company")
    @Mapping(source = "skills",  target = "skillNames", qualifiedByName = "toSkillNames")
    JobResponse toResponseLite(Job job);

    @AfterMapping
    default void fillSalaryDisplay(Job source, @MappingTarget JobResponse.JobResponseBuilder target) {
        Integer t = source.getSalaryType(); // 0=range, 1=negotiable, 2=hidden
        if (t == null) return;
        switch (t) {
            case 0 -> {
                Long min = source.getSalaryMin(), max = source.getSalaryMax();
                if (min != null && max != null) {
                    target.salaryDisplay(String.format("%,d - %,d VND", min, max));
                }
            }
            case 1 -> target.salaryDisplay("Thỏa thuận");
            case 2 -> target.salaryDisplay("Ẩn lương");
        }
    }


    @Named("toCategoryNames")
    default List<String> toCategoryNames(Set<Category> s) {
        return (s == null || s.isEmpty()) ? List.of()
                : s.stream().map(Category::getName).collect(Collectors.toList());
    }
    @Named("toSkillNames")
    default List<String> toSkillNames(Set<Skill> s) {
        return (s == null || s.isEmpty()) ? List.of()
                : s.stream().map(Skill::getName).collect(Collectors.toList());
    }
    @Named("toLevelNames")
    default List<String> toLevelNames(Set<Level> s) {
        return (s == null || s.isEmpty()) ? List.of()
                : s.stream().map(Level::getName).collect(Collectors.toList());
    }
    @Named("toWorkTypeNames")
    default List<String> toWorkTypeNames(Set<WorkType> s) {
        return (s == null || s.isEmpty()) ? List.of()
                : s.stream().map(WorkType::getName).collect(Collectors.toList());
    }
}
