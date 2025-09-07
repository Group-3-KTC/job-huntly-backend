package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.response.UserDto;
import com.jobhuntly.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserDto> findAllByRole(String role, Pageable pageable);
    Page<UserDto> findAll(Pageable pageable);
}
