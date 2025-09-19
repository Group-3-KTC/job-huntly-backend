package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.dto.request.JobFilterRequest;
import com.jobhuntly.backend.entity.Job;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public final class JobSpecifications {

    private JobSpecifications() {}
    public static Specification<Job> build(JobFilterRequest r) {
        List<Specification<Job>> specs = new ArrayList<>();

        // Chuẩn hoá input 1 lần (hạn chế lower() lặp lại)
        String keywordLc      = lc(r.getKeyword());
        String companyNameLc  = lc(r.getCompanyName());
        String cityNameLc     = lc(r.getCityName());

        Set<String> catsLc    = lcSet(r.getCategoryNames());
        Set<String> skillsLc  = lcSet(r.getSkillNames());
        Set<String> levelsLc  = lcSet(r.getLevelNames());
        Set<String> wtypesLc  = lcSet(r.getWorkTypeNames());
        Set<String> wardsLc   = lcSet(r.getWardNames());

        // Keyword & Company
        if (hasText(keywordLc))     specs.add(titleContainsLc(keywordLc));
        if (hasText(companyNameLc)) specs.add(companyNameEqualsLc(companyNameLc));

        // City: job có ít nhất một ward thuộc city
        if (hasText(cityNameLc))    specs.add(anyWardInCityExists(cityNameLc));

        // Salary & Posted date
        if (r.getSalaryMin() != null) specs.add(salaryMinAtLeast(r.getSalaryMin()));
        if (r.getSalaryMax() != null) specs.add(salaryMaxAtMost(r.getSalaryMax()));
        if (r.getPostedFrom() != null || r.getPostedTo() != null)
            specs.add(postedBetween(r.getPostedFrom(), r.getPostedTo()));

        // Status
        if (hasText(r.getStatus())) specs.add(statusEqualsLc(lc(r.getStatus())));

        // Only active
        if (Boolean.TRUE.equals(r.getOnlyActive())) specs.add(notExpired());

        // Many-to-many by name (ANY/ALL) dùng EXISTS—không dùng DISTINCT
        if (!catsLc.isEmpty())   specs.add(mtmByNameExists("categories", "name",   catsLc,  r.isMatchAllCategories()));
        if (!skillsLc.isEmpty()) specs.add(mtmByNameExists("skills",     "name",   skillsLc,r.isMatchAllSkills()));
        if (!levelsLc.isEmpty()) specs.add(mtmByNameExists("levels",     "name",   levelsLc,r.isMatchAllLevels()));
        if (!wtypesLc.isEmpty()) specs.add(mtmByNameExists("workTypes",  "name",   wtypesLc,r.isMatchAllWorkTypes()));
        if (!wardsLc.isEmpty())  specs.add(mtmByNameExists("wards",      "name",   wardsLc, r.isMatchAllWards()));

        // AND tất cả điều kiện (không dùng Specification.where(null) — đã deprecated)
        return specs.isEmpty() ? null : Specification.allOf(specs);
    }
    private static Specification<Job> titleContainsLc(String keywordLc) {
        return (root, cq, cb) -> cb.like(cb.lower(root.get("title")), "%" + keywordLc + "%");
    }

    private static Specification<Job> companyNameEqualsLc(String companyNameLc) {
        return (root, cq, cb) -> cb.equal(cb.lower(root.get("company").get("companyName")), companyNameLc);
    }

    private static Specification<Job> statusEqualsLc(String statusLc) {
        return (root, cq, cb) -> cb.equal(cb.lower(root.get("status")), statusLc);
    }

    private static Specification<Job> notExpired() {
        return (root, cq, cb) -> cb.or(
                cb.isNull(root.get("expiredDate")),
                cb.greaterThanOrEqualTo(root.get("expiredDate"), LocalDate.now())
        );
    }

    private static Specification<Job> salaryMinAtLeast(Long min) {
        return (root, cq, cb) -> cb.or(
                cb.isNull(root.get("salaryMin")),
                cb.greaterThanOrEqualTo(root.get("salaryMin"), min)
        );
    }

    private static Specification<Job> salaryMaxAtMost(Long max) {
        return (root, cq, cb) -> cb.or(
                cb.isNull(root.get("salaryMax")),
                cb.lessThanOrEqualTo(root.get("salaryMax"), max)
        );
    }

    private static Specification<Job> postedBetween(LocalDate from, LocalDate to) {
        return (root, cq, cb) -> {
            if (from != null && to != null) return cb.between(root.get("datePost"), from, to);
            if (from != null)               return cb.greaterThanOrEqualTo(root.get("datePost"), from);
            if (to != null)                 return cb.lessThanOrEqualTo(root.get("datePost"), to);
            return cb.conjunction();
        };
    }
    private static Specification<Job> anyWardInCityExists(String cityNameLc) {
        return (root, cq, cb) -> {
            Subquery<Long> sq = cq.subquery(Long.class);
            Root<Job> j2 = sq.from(Job.class);
            Join<Object, Object> w = j2.join("wards"); // INNER JOIN
            Join<Object, Object> c = w.join("city");   // INNER JOIN

            sq.select(j2.get("id"))
                    .where(
                            cb.equal(j2.get("id"), root.get("id")),
                            cb.equal(cb.lower(c.get("name")), cityNameLc)
                    );
            return cb.exists(sq);
        };
    }

    private static Specification<Job> mtmByNameExists(String attr, String nameField, Set<String> namesLc, boolean matchAll) {
        if (namesLc == null || namesLc.isEmpty()) return (root, cq, cb) -> cb.conjunction();

        if (!matchAll) {
            return (root, cq, cb) -> {
                Subquery<Long> sq = cq.subquery(Long.class);
                Root<Job> j2 = sq.from(Job.class);
                Join<Object, Object> jn = j2.join(attr); // INNER JOIN
                Expression<String> nameExpr = cb.lower(jn.get(nameField));
                sq.select(j2.get("id"))
                        .where(
                                cb.equal(j2.get("id"), root.get("id")),
                                nameExpr.in(namesLc)
                        );
                return cb.exists(sq);
            };
        } else {
            List<Specification<Job>> perName = namesLc.stream()
                    .map(n -> (Specification<Job>) (root, cq, cb) -> {
                        Subquery<Long> sq = cq.subquery(Long.class);
                        Root<Job> j2 = sq.from(Job.class);
                        Join<Object, Object> jn = j2.join(attr);

                        sq.select(j2.get("id"))
                                .where(
                                        cb.equal(j2.get("id"), root.get("id")),
                                        cb.equal(cb.lower(jn.get(nameField)), n)
                                );
                        return cb.exists(sq);
                    })
                    .toList();

            return Specification.allOf(perName);
        }
    }
    private static boolean hasText(String s) { return s != null && !s.isBlank(); }

    private static String lc(String s) { return (s == null) ? null : s.toLowerCase(Locale.ROOT).trim(); }

    private static Set<String> lcSet(Set<String> in) {
        if (in == null || in.isEmpty()) return Collections.emptySet();
        return in.stream()
                .filter(Objects::nonNull)
                .map(str -> str.toLowerCase(Locale.ROOT).trim())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}