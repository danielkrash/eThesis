package com.uni.ethesis.utils.mappers;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.uni.ethesis.data.dto.TeacherDto;
import com.uni.ethesis.data.entities.Teacher;
import com.uni.ethesis.web.view.model.TeacherViewModel;

/**
 * Unified mapper for Teacher entity, DTO, and ViewModel conversions.
 * This replaces both TeacherMapper and TeacherViewModelMapper.
 */
@Mapper(componentModel = "spring")
public interface TeacherMapper {

    // ============================================
    // Entity ↔ DTO Mappings
    // ============================================
    
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    TeacherDto teacherToTeacherDto(Teacher teacher);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teacherThesisProposals", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "defenseSessions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    Teacher teacherDtoToTeacher(TeacherDto teacherDto);

    // ============================================
    // DTO ↔ ViewModel Mappings
    // ============================================
    
    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    TeacherViewModel toViewModel(TeacherDto dto);

    @Mapping(source = "id", target = "id", qualifiedByName = "stringToUuid")
    TeacherDto toDto(TeacherViewModel viewModel);

    // List mappings
    List<TeacherViewModel> toViewModels(List<TeacherDto> dtos);
    List<TeacherDto> toDtos(List<TeacherViewModel> viewModels);

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
