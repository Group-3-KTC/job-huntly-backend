package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.response.UserDto;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.mapper.UserMapper;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Page<UserDto> findAllByRole(String role, Pageable pageable) {
        Page<User> usersPage = userRepository.findAllByRole(role, pageable);
        return usersPage.map(userMapper::toDto);
    }

    @Override
    public Page<UserDto> findAll(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);
        return usersPage.map(userMapper::toDto);
    }
}
