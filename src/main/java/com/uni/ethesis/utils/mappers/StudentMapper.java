package com.uni.ethesis.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.uni.ethesis.data.dto.StudentDto;
import com.uni.ethesis.data.entities.Student;

/**
 * Unified mapper for Student entity and DTO conversions.
 * No ViewModel mappings since StudentViewModel doesn't exist.
 */
@Mapper(componentModel = "spring")
public interface StudentMapper {

    // ============================================
    // Entity ↔ DTO Mappings
    // ============================================
    
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    // No explicit mapping needed for universityId if field names match in DTO and Entity
    // MapStruct handles direct field name matches automatically.
    StudentDto studentToStudentDto(Student student);

    @Mapping(target = "user", ignore = true) // User mapping is complex, handled in service
    @Mapping(target = "id", ignore = true) // ID is often DB generated or from User (@MapsId)
    @Mapping(target = "studentThesisProposals", ignore = true)
    @Mapping(target = "studentType", ignore = true) // Add mapping if StudentDto contains studentType
    @Mapping(target = "createdAt", ignore = true) // Ignore audit field
    @Mapping(target = "lastModifiedAt", ignore = true) // Ignore audit field
    // No explicit mapping needed for universityId if field names match in DTO and Entity
    Student studentDtoToStudent(StudentDto studentDto);

    // If you need to update an existing Student entity from a DTO, you'd use @MappingTarget:
    // void updateStudentFromDto(StudentDto studentDto, @MappingTarget Student student);
}
