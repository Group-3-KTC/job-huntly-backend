package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.response.UserDto;
import com.jobhuntly.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDto> usersPage;
        
        if (role != null && !role.isEmpty()) {
            usersPage = userService.findAllByRole(role, pageable);
        } else {
            usersPage = userService.findAll(pageable);
        }
        return new ResponseEntity<>(usersPage, HttpStatus.OK);
    }
}
