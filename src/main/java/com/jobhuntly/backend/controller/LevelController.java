package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.response.LevelResponse;
import com.jobhuntly.backend.service.LevelService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("${backend.prefix}/levels")
public class LevelController {
    private final LevelService levelService;

    @GetMapping
    public List<LevelResponse> getAllLevels() {
        return levelService.getAllLevels();
    }
}
