package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.UserRequest;
import com.jobhuntly.backend.dto.response.HasCompanyResponse;
import com.jobhuntly.backend.dto.response.UserDto;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.service.UserService;
import jakarta.validation.Valid;
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

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto userDto = userService.getUserById(id);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getInfoCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        UserDto userDto = userService.getUserById(userId);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }
    
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserDto createdUser = userService.createUser(userRequest);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @RequestBody UserRequest userRequest) {
        UserDto updatedUser = userService.updateUserById(id, userRequest);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/has-company")
    public ResponseEntity<HasCompanyResponse> hasCompany() {
        HasCompanyResponse response = userService.hasCompany();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
