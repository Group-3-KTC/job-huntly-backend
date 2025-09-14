package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.FollowRequest;
import com.jobhuntly.backend.dto.response.FollowCountResponse;
import com.jobhuntly.backend.dto.response.FollowResponse;
import com.jobhuntly.backend.dto.response.FollowStatusResponse;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.service.FollowService;
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
@RequestMapping("${backend.prefix}/follows")
public class FollowController {
    private final FollowService followService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody FollowRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        followService.create(userId, request.getCompanyId());
    }


    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam("company_id") Long companyId) {
        Long userId = SecurityUtils.getCurrentUserId();
        followService.delete(userId, companyId);
    }

    @GetMapping("/count")
    public FollowCountResponse count(@RequestParam("company_id") Long companyId) {
        long total = followService.countFollowers(companyId);
        return new FollowCountResponse(companyId, total);
    }

    @GetMapping("/by-user")
    public Page<FollowResponse> getFollowedCompaniesByCurrentUser(
            @PageableDefault(size = 10, sort = "followId", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        return followService.getFollowedCompanies(userId, pageable);
    }

    @GetMapping("/status")
    public FollowStatusResponse status(@RequestParam("company_id") Long companyId) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean followed = followService.isFollowed(userId, companyId);
        return new FollowStatusResponse(followed);
    }
}
