package com.uni.ethesis.utils.mappers;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.uni.ethesis.data.dto.ProfileUpdateDto;
import com.uni.ethesis.data.dto.UserDto;
import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.web.view.model.ProfileUpdateViewModel;
import com.uni.ethesis.web.view.model.UserViewModel;

/**
 * Unified mapper for User entity, DTO, and ViewModel conversions.
 * This replaces both UserMapper and UserViewModelMapper.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    // ============================================
    // Entity ↔ DTO Mappings
    // ============================================
    
    UserDto userToUserDto(User user);

    User userDtoToUser(UserDto userDto);

    // ============================================
    // DTO ↔ ViewModel Mappings
    // ============================================
    
    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    @Mapping(target = "role", ignore = true) // Role will be set separately from Authentication
    UserViewModel toViewModel(UserDto dto);

    @Mapping(source = "id", target = "id", qualifiedByName = "stringToUuid")
    UserDto toDto(UserViewModel viewModel);

    // List mappings
    List<UserViewModel> toViewModels(List<UserDto> dtos);
    List<UserDto> toDtos(List<UserViewModel> viewModels);

    // ============================================
    // Profile Update Mappings
    // ============================================
    
    ProfileUpdateDto toProfileUpdateDto(ProfileUpdateViewModel viewModel);
    ProfileUpdateViewModel toProfileUpdateViewModel(ProfileUpdateDto dto);

    // ============================================
    // Helper Methods
    // ============================================
    
    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("stringToUuid")
    default UUID stringToUuid(String str) {
        return str != null && !str.isEmpty() ? UUID.fromString(str) : null;
    }
}
