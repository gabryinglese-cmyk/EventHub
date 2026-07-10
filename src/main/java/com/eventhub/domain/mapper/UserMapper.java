package com.eventhub.domain.mapper;

import org.mapstruct.Mapper;
import com.eventhub.domain.dto.UserDto;
import com.eventhub.domain.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto userDto);
}