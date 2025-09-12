package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.SkillRequest;
import com.jobhuntly.backend.dto.response.SkillResponse;
import com.jobhuntly.backend.entity.Category;
import com.jobhuntly.backend.entity.Skill;
import com.jobhuntly.backend.mapper.SkillMapper;
import com.jobhuntly.backend.repository.CategoryRepository;
import com.jobhuntly.backend.repository.SkillRepository;
import com.jobhuntly.backend.service.SkillService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.jobhuntly.backend.constant.CacheConstant.DICT_SKILLS;

@Service
@AllArgsConstructor
@Transactional
public class SkillServiceImpl implements SkillService {
    private final SkillRepository skillRepository;
    private final CategoryRepository categoryRepository;
    private final SkillMapper skillMapper;

    @Override
    public SkillResponse createSkill(SkillRequest request) {
        String skillName = normalize(request.getName());
        if (skillName.isBlank()) {
            throw new IllegalArgumentException("Skill name is reqiured");
        }
        if (skillRepository.existsByNameIgnoreCase(skillName)) {
            throw new IllegalArgumentException("Skill already exists");
        }

        List<String> names = Optional.ofNullable(request.getCategoryNames()).orElseGet(List::of);
        Set<String> normalized = names.stream()
                .map(this::normalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("At least one category name is required");
        }

        // Find existing category
        Set<Category> categories = normalized.stream()
                .map(n -> categoryRepository.findByNameIgnoreCase(n).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (categories.isEmpty()) {
            throw new IllegalArgumentException("No valid categories found");
        }
        Skill skill = skillMapper.toEntity(request);
        skill.setName(skillName);
        skill.setCategories(categories);

        Skill saved = skillRepository.save(skill);
        return skillMapper.toResponse(saved);
    }

    @Override
    @Cacheable(cacheNames = DICT_SKILLS, key = "#categoryName", unless = "#result == null || #result.isEmpty()")
    public List<SkillResponse> getSkillsByCategoryName(String categoryName) {
        String rootName = normalize(categoryName);
        if (rootName.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }

        // 1) Tìm category theo tên (ignore case)
        Category root = categoryRepository.findByNameIgnoreCase(rootName)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + rootName));

        // 2) Lấy children theo tên cha
        List<Category> children = categoryRepository.findAllByParent_NameIgnoreCase(root.getName());

        List<String> targetNames = new ArrayList<>();
        targetNames.add(root.getName());
        for (Category c : children) {
            targetNames.add(c.getName());
        }

        List<String> names = targetNames.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().replaceAll("\\s+", " "))
                .distinct()
                .toList();

        List<Skill> skills;
        if (names.isEmpty()) {
            skills = List.of();
        } else if (names.size() == 1) {
            skills = skillRepository.findDistinctByCategories_NameIgnoreCase(names.get(0));
        } else {
            skills = skillRepository.findDistinctByCategories_NameIn(names);
        }

        return skillMapper.toListSkill(skills);
    }

    private String normalize(String raw) {
        return raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
    }
}

