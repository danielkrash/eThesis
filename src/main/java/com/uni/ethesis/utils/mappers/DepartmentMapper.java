package com.uni.ethesis.utils.mappers;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.uni.ethesis.data.dto.DepartmentDto;
import com.uni.ethesis.data.entities.Department;
import com.uni.ethesis.web.view.model.DepartmentViewModel;

/**
 * Unified mapper for Department entity, DTO, and ViewModel conversions.
 * This replaces both DepartmentMapper and DepartmentViewModelMapper.
 */
@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    DepartmentMapper INSTANCE = Mappers.getMapper(DepartmentMapper.class);

    // ============================================
    // Entity ↔ DTO Mappings
    // ============================================
    
    DepartmentDto departmentToDepartmentDto(Department department);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "defenses", ignore = true)
    @Mapping(target = "proposals", ignore = true) // Proposals are not set from DTO
    Department departmentDtoToDepartment(DepartmentDto departmentDto);

    @Mapping(target = "proposals", ignore = true) // Proposals are not set from DTO
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "defenses", ignore = true)
    @Mapping(target = "id", ignore = true) // Usually ID is not updated from DTO
    void updateDepartmentFromDto(DepartmentDto departmentDto, @MappingTarget Department department);

    // ============================================
    // DTO ↔ ViewModel Mappings
    // ============================================
    
    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    DepartmentViewModel toViewModel(DepartmentDto dto);

    @Mapping(source = "id", target = "id", qualifiedByName = "stringToUuid")
    DepartmentDto toDto(DepartmentViewModel viewModel);

    // List mappings
    List<DepartmentViewModel> toViewModels(List<DepartmentDto> dtos);
    List<DepartmentDto> toDtos(List<DepartmentViewModel> viewModels);

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
