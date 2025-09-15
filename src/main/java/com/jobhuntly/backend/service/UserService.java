package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.UserRequest;
import com.jobhuntly.backend.dto.response.HasCompanyResponse;
import com.jobhuntly.backend.dto.response.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserDto> findAllByRole(String role, Pageable pageable);
    Page<UserDto> findAll(Pageable pageable);

    UserDto getUserById(Long id);
    UserDto createUser(UserRequest userRequest);
    UserDto updateUserById(Long id, UserRequest userRequest);
    void deleteUserById(Long id);
    
    HasCompanyResponse hasCompany();
}
