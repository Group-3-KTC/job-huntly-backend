package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.ApplicationRequest;
import com.jobhuntly.backend.dto.response.ApplicationByUserResponse;
import com.jobhuntly.backend.dto.response.ApplicationResponse;
import com.jobhuntly.backend.security.jwt.JwtUtil;
import com.jobhuntly.backend.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/application")
public class ApplicationController {
    private final ApplicationService applicationService;
    private final JwtUtil jwtUtil;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse create(@Valid @ModelAttribute ApplicationRequest request,
                                      @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return applicationService.create(userId,request);
    }

    @GetMapping(value = "/by-user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ApplicationByUserResponse> getByUser(@RequestHeader("Authorization") String authHeader,
                                                     @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                                                     Pageable pageable
                                                     ) {
        Long userId = extractUserId(authHeader);
        return applicationService.getByUser(userId, pageable);
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractUserId(token);
    }

}
