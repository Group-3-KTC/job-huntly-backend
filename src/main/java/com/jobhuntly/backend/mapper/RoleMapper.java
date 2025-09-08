package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.entity.Role;
import com.jobhuntly.backend.repository.RoleRepository;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {
    
    private final RoleRepository roleRepository;
    
    public RoleMapper(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    
    @Named("roleIdToRole")
    public Role roleIdToRole(Integer roleId) {
        if (roleId == null) {
            return null;
        }
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại với ID: " + roleId));
    }
} 