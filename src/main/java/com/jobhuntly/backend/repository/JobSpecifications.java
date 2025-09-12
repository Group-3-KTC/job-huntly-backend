package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.dto.request.JobFilterRequest;
import com.jobhuntly.backend.entity.Job;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class JobSpecifications {

    private JobSpecifications() {}

    public static Specification<Job> build(JobFilterRequest r) {
        List<Specification<Job>> specs = new ArrayList<>();

        // Keyword & Company
        if (hasText(r.getKeyword()))     specs.add(titleContains(r.getKeyword()));
        if (hasText(r.getCompanyName())) specs.add(companyNameEquals(r.getCompanyName()));

        // City -> job có bất kỳ ward thuộc city đó
        if (hasText(r.getCityName()))    specs.add(anyWardInCity(r.getCityName()));

        // Salary & Posted date
        if (r.getSalaryMin() != null)    specs.add(salaryMinAtLeast(r.getSalaryMin()));
        if (r.getSalaryMax() != null)    specs.add(salaryMaxAtMost(r.getSalaryMax()));
        if (r.getPostedFrom() != null || r.getPostedTo() != null)
            specs.add(postedBetween(r.getPostedFrom(), r.getPostedTo()));

        // Status
        if (hasText(r.getStatus()))      specs.add(statusEqualsIgnoreCase(r.getStatus()));

        // Only active (chưa hết hạn)
        if (Boolean.TRUE.equals(r.getOnlyActive())) specs.add(notExpired());

        // ManyToMany theo name
        if (notEmpty(r.getCategoryNames()))
            specs.add(mtmByName("categories", "name", r.getCategoryNames(), r.isMatchAllCategories()));
        if (notEmpty(r.getSkillNames()))
            specs.add(mtmByName("skills", "name", r.getSkillNames(), r.isMatchAllSkills()));
        if (notEmpty(r.getLevelNames()))
            specs.add(mtmByName("levels", "name", r.getLevelNames(), r.isMatchAllLevels()));
        if (notEmpty(r.getWorkTypeNames()))
            specs.add(mtmByName("workTypes", "name", r.getWorkTypeNames(), r.isMatchAllWorkTypes()));
        if (notEmpty(r.getWardNames()))
            specs.add(mtmByName("wards", "name", r.getWardNames(), r.isMatchAllWards()));

        // AND tất cả điều kiện
        return Specification.allOf(specs);
    }

    // --- job có ward thuộc cityName (ignore-case) ---
    public static Specification<Job> anyWardInCity(String cityName) {
        return (root, cq, cb) -> {
            Join<Object, Object> wardJoin = root.join("wards", JoinType.LEFT);
            Join<Object, Object> cityJoin = wardJoin.join("city", JoinType.LEFT);
            cq.distinct(true);
            return cb.equal(cb.lower(cityJoin.get("name")), cityName.toLowerCase());
        };
    }

    // --- primitives ---
    private static boolean hasText(String s) { return s != null && !s.isBlank(); }
    private static boolean notEmpty(Set<?> s) { return s != null && !s.isEmpty(); }

    public static Specification<Job> titleContains(String keyword) {
        return (root, cq, cb) -> cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<Job> companyNameEquals(String name) {
        // Đổi "name" nếu field Company khác
        return (root, cq, cb) -> cb.equal(cb.lower(root.get("company").get("name")), name.toLowerCase());
    }

    public static Specification<Job> statusEqualsIgnoreCase(String status) {
        return (root, cq, cb) -> cb.equal(cb.lower(root.get("status")), status.toLowerCase());
    }

    public static Specification<Job> notExpired() {
        return (root, cq, cb) -> cb.or(
                cb.isNull(root.get("expiredDate")),
                cb.greaterThanOrEqualTo(root.get("expiredDate"), LocalDate.now())
        );
    }

    public static Specification<Job> salaryMinAtLeast(Long min) {
        return (root, cq, cb) -> cb.or(
                cb.isNull(root.get("salaryMin")),
                cb.greaterThanOrEqualTo(root.get("salaryMin"), min)
        );
    }

    public static Specification<Job> salaryMaxAtMost(Long max) {
        return (root, cq, cb) -> cb.or(
                cb.isNull(root.get("salaryMax")),
                cb.lessThanOrEqualTo(root.get("salaryMax"), max)
        );
    }

    public static Specification<Job> postedBetween(LocalDate from, LocalDate to) {
        return (root, cq, cb) -> {
            if (from != null && to != null) return cb.between(root.get("datePost"), from, to);
            if (from != null)               return cb.greaterThanOrEqualTo(root.get("datePost"), from);
            if (to != null)                 return cb.lessThanOrEqualTo(root.get("datePost"), to);
            return cb.conjunction();
        };
    }

    // --- ManyToMany theo name: ANY/ALL ---
    private static Specification<Job> mtmByName(String attr, String nameField, Set<String> names, boolean matchAll) {
        // Phòng thủ: nếu rỗng, trả về "true" predicate
        if (names == null || names.isEmpty()) {
            return (root, cq, cb) -> cb.conjunction();
        }

        if (!matchAll) {
            // ANY
            return (root, cq, cb) -> {
                Join<Object, Object> j = root.join(attr, JoinType.LEFT);
                cq.distinct(true);
                CriteriaBuilder.In<String> in = cb.in(cb.lower(j.get(nameField)));
                names.stream().map(String::toLowerCase).forEach(in::value);
                return in;
            };
        } else {
            // ALL: đếm số tên khớp == size(names)
            return (root, cq, cb) -> {
                Subquery<Long> sq = cq.subquery(Long.class);
                Root<Job> j2 = sq.from(Job.class);
                Join<Object, Object> join = j2.join(attr, JoinType.LEFT);

                sq.select(cb.countDistinct(cb.lower(join.get(nameField))))
                        .where(
                                cb.equal(j2.get("id"), root.get("id")),
                                cb.lower(join.get(nameField)).in(names.stream().map(String::toLowerCase).toList())
                        );
                return cb.equal(sq, (long) names.size());
            };
        }
    }
}