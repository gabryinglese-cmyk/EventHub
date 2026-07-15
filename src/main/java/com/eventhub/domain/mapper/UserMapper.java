package com.eventhub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.eventhub.domain.dto.CreateUserRequest;
import com.eventhub.domain.dto.UserDto;
import com.eventhub.domain.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(CreateUserRequest createUserRequest);
}