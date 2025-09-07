package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.response.UserDto;
import com.jobhuntly.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roleName", source = "role.roleName")
    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> users);
}
