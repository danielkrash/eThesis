package com.uni.ethesis.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.uni.ethesis.data.dto.DepartmentDto;
import com.uni.ethesis.data.entities.Department;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    DepartmentMapper INSTANCE = Mappers.getMapper(DepartmentMapper.class);

    DepartmentDto departmentToDepartmentDto(Department department);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "defenses", ignore = true)
    Department departmentDtoToDepartment(DepartmentDto departmentDto);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "defenses", ignore = true)
    @Mapping(target = "id", ignore = true) // Usually ID is not updated from DTO
    void updateDepartmentFromDto(DepartmentDto departmentDto, @MappingTarget Department department);
}
