package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.UserRequest;
import com.jobhuntly.backend.dto.response.UserDto;
import com.jobhuntly.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {
    @Mapping(target = "roleName", source = "role.roleName")
    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> users);
    
    @Mapping(target = "role", source = "roleId", qualifiedByName = "roleIdToRole")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "company", ignore = true)
    User toEntity(UserRequest userRequest);
    
    @Mapping(target = "role", source = "roleId", qualifiedByName = "roleIdToRole")
    @Mapping(target = "passwordHash", source = "password")
    void updateUserFromDto(UserRequest userRequest, @MappingTarget User user);
}
