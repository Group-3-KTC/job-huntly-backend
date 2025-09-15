package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.UserRequest;
import com.jobhuntly.backend.dto.response.HasCompanyResponse;
import com.jobhuntly.backend.dto.response.UserDto;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.exception.ResourceNotFoundException;
import com.jobhuntly.backend.mapper.RoleMapper;
import com.jobhuntly.backend.mapper.UserMapper;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

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

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại với ID: " + id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto createUser(UserRequest userRequest) {
        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }
        
        User user = userMapper.toEntity(userRequest);
        
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        }
        
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUserById(Long id, UserRequest userRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại với ID: " + id));
        
        if (userRequest.getEmail() != null) {
            if (!existingUser.getEmail().equals(userRequest.getEmail()) && 
                    userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email đã được sử dụng");
            }
            existingUser.setEmail(userRequest.getEmail());
        }
        
        if (userRequest.getFullName() != null) {
            existingUser.setFullName(userRequest.getFullName());
        }
        
        if (userRequest.getPhone() != null) {
            existingUser.setPhone(userRequest.getPhone());
        }
        
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            existingUser.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        }
        
        if (userRequest.getGoogleId() != null) {
            existingUser.setGoogleId(userRequest.getGoogleId());
        }
        
        if (userRequest.getIsActive() != null) {
            existingUser.setIsActive(userRequest.getIsActive());
        }
        
        if (userRequest.getStatus() != null) {
            existingUser.setStatus(userRequest.getStatus());
        }
        
        if (userRequest.getRoleId() != null) {
            existingUser.setRole(roleMapper.roleIdToRole(userRequest.getRoleId()));
        }
        
        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User không tồn tại với ID: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public HasCompanyResponse hasCompany() {
        Long userId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại với ID: " + userId));
        
        HasCompanyResponse.HasCompanyResponseBuilder responseBuilder = HasCompanyResponse.builder()
                .userId(userId)
                .hasCompany(false);

        if (user.getCompany() != null) {
            responseBuilder
                    .hasCompany(true)
                    .companyId(user.getCompany().getId())
                    .companyName(user.getCompany().getCompanyName())
                    .message("User already has company");
        } else {
            responseBuilder.message("User does not have company");
        }

        return responseBuilder.build();
    }
}
