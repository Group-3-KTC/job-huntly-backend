package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.ApplicationRequest;
import com.jobhuntly.backend.dto.response.ApplicationByUserResponse;
import com.jobhuntly.backend.dto.response.ApplicationResponse;
import com.jobhuntly.backend.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/application")
public class ApplicationController {
    private final ApplicationService applicationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse create(@Valid @ModelAttribute ApplicationRequest request) {
        return applicationService.create(request);
    }

    @GetMapping(value = "/by-user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ApplicationByUserResponse> getByUser(@PathVariable Long userId,
                                                     @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                                                     Pageable pageable
                                                     ) {
        return applicationService.getByUser(userId, pageable);
    }

}
