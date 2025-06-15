package com.uni.ethesis.utils.mappers;

import com.uni.ethesis.data.dto.UserDto;
import com.uni.ethesis.data.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto userToUserDto(User user);

    User userDtoToUser(UserDto userDto);
}
