package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.response.WorkTypeResponse;
import com.jobhuntly.backend.service.WorkTypeService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("${backend.prefix}/worktypes")
public class WorkTypeController {
    private final WorkTypeService workTypeService;

    @GetMapping
    public List<WorkTypeResponse> getAllWorkTypes() {
        return workTypeService.getAllWorkType();
    }
}
