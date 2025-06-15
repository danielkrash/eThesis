package com.uni.ethesis.utils.mappers;

import com.uni.ethesis.data.dto.UserDto;
import com.uni.ethesis.data.dto.ProfileUpdateDto;
import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.web.view.model.UserViewModel;
import com.uni.ethesis.web.view.model.ProfileUpdateViewModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto userToUserDto(User user);

    User userDtoToUser(UserDto userDto);

    // View Model mapping methods
    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    @Mapping(target = "role", ignore = true) // Role will be set separately from Authentication
    UserViewModel toViewModel(UserDto dto);

    // Profile Update mapping
    ProfileUpdateDto toProfileUpdateDto(ProfileUpdateViewModel viewModel);

    @Named("uuidToString")
    default String uuidToString(java.util.UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }
}
