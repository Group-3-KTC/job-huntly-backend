package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.SkillRequest;
import com.jobhuntly.backend.dto.response.SkillResponse;
import com.jobhuntly.backend.entity.Skill;
import com.jobhuntly.backend.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/skill")
public class SkillController {
    private final SkillService skillService;

    @PostMapping("/create")
    public ResponseEntity<?> createSkill(@RequestBody SkillRequest request) {
        try {
            SkillResponse res = skillService.createSkill(request);
            return ResponseEntity.ok(res);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Constraint violated (duplicate or FK).");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error");
        }
    }

    @GetMapping("/by-category")
    public ResponseEntity<?> getByCategory(@RequestParam("name") String categoryName) {
        try {
            return ResponseEntity.ok(skillService.getSkillsByCategoryName(categoryName));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
