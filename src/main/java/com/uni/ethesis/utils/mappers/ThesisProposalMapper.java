package com.uni.ethesis.utils.mappers;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.data.entities.Department;
import com.uni.ethesis.data.entities.Student;
import com.uni.ethesis.data.entities.Teacher;
import com.uni.ethesis.data.entities.ThesisProposal;
import com.uni.ethesis.data.repo.DepartmentRepository;
import com.uni.ethesis.data.repo.StudentRepository;
import com.uni.ethesis.data.repo.TeacherRepository;
import com.uni.ethesis.enums.ThesisProposalStatus;
import com.uni.ethesis.exceptions.StudentNotFoundException;
import com.uni.ethesis.exceptions.TeacherNotFoundException;
import com.uni.ethesis.web.view.model.CreateThesisProposalViewModel;
import com.uni.ethesis.web.view.model.ThesisProposalViewModel;

/**
 * Unified mapper for ThesisProposal entity, DTO, and ViewModel conversions.
 * This replaces both ThesisProposalMapper and ThesisProposalViewModelMapper.
 */
@Mapper(componentModel = "spring", uses = {StudentRepository.class, TeacherRepository.class})
public interface ThesisProposalMapper {

    ThesisProposalMapper INSTANCE = Mappers.getMapper(ThesisProposalMapper.class);

    // ============================================
    // Entity ↔ DTO Mappings
    // ============================================
    
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "teacher.id", target = "teacherId")
    ThesisProposalDto thesisProposalToThesisProposalDto(ThesisProposal thesisProposal);

    @Mapping(target = "department", source = "departmentId", qualifiedByName = "departmentFromId")
    @Mapping(source = "studentId", target = "student", qualifiedByName = "studentFromId")
    @Mapping(source = "teacherId", target = "teacher", qualifiedByName = "teacherFromId")
    @Mapping(target = "thesis", ignore = true) // Thesis is created later, not from DTO
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    ThesisProposal thesisProposalDtoToThesisProposal(ThesisProposalDto thesisProposalDto, @Context StudentRepository studentRepository, @Context TeacherRepository teacherRepository, @Context DepartmentRepository departmentRepository);

    @Mapping(target = "department", source = "departmentId", qualifiedByName = "departmentFromId")
    @Mapping(source = "studentId", target = "student", qualifiedByName = "studentFromId")
    @Mapping(source = "teacherId", target = "teacher", qualifiedByName = "teacherFromId")
    @Mapping(target = "thesis", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateThesisProposalFromDto(ThesisProposalDto thesisProposalDto, @MappingTarget ThesisProposal thesisProposal, @Context StudentRepository studentRepository, @Context TeacherRepository teacherRepository, @Context DepartmentRepository departmentRepository);

    // ============================================
    // DTO ↔ ViewModel Mappings
    // ============================================
    
    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    @Mapping(source = "studentId", target = "studentId", qualifiedByName = "uuidToString")
    @Mapping(source = "teacherId", target = "teacherId", qualifiedByName = "uuidToString")
    @Mapping(source = "departmentId", target = "departmentId", qualifiedByName = "uuidToString")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    @Mapping(source = "status", target = "statusDisplayName", qualifiedByName = "statusToDisplayName")
    @Mapping(target = "studentName", ignore = true) // Set manually in service
    @Mapping(target = "teacherName", ignore = true) // Set manually in service
    @Mapping(target = "canEdit", ignore = true) // Set manually in service
    @Mapping(target = "canApply", ignore = true) // Set manually in service
    @Mapping(target = "canApprove", ignore = true) // Set manually in service
    @Mapping(target = "canReject", ignore = true) // Set manually in service
    ThesisProposalViewModel toViewModel(ThesisProposalDto dto);

    @Mapping(source = "id", target = "id", qualifiedByName = "stringToUuid")
    @Mapping(source = "studentId", target = "studentId", qualifiedByName = "stringToUuid")
    @Mapping(source = "teacherId", target = "teacherId", qualifiedByName = "stringToUuid")
    @Mapping(source = "departmentId", target = "departmentId", qualifiedByName = "stringToUuid")
    @Mapping(source = "status", target = "status", qualifiedByName = "stringToStatus")
    ThesisProposalDto toDto(ThesisProposalViewModel viewModel);

    // List mappings
    List<ThesisProposalViewModel> toViewModels(List<ThesisProposalDto> dtos);
    List<ThesisProposalDto> toDtos(List<ThesisProposalViewModel> viewModels);

    // ============================================
    // CreateViewModel ↔ DTO Mappings
    // ============================================
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "studentId", ignore = true)
    @Mapping(source = "teacherId", target = "teacherId", qualifiedByName = "stringToUuid")
    @Mapping(source = "departmentId", target = "departmentId", qualifiedByName = "stringToUuid")
    @Mapping(target = "status", ignore = true) // Set to PENDING in service
    @Mapping(target = "createdAt", ignore = true) // Will be set by JPA auditing
    @Mapping(target = "lastModifiedAt", ignore = true) // Will be set by JPA auditing
    ThesisProposalDto fromCreateViewModel(CreateThesisProposalViewModel viewModel);

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

    @Named("statusToString")
    default String statusToString(ThesisProposalStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToStatus")
    default ThesisProposalStatus stringToStatus(String status) {
        return status != null ? ThesisProposalStatus.valueOf(status) : null;
    }

    @Named("statusToDisplayName")
    default String statusToDisplayName(ThesisProposalStatus status) {
        if (status == null) return null;
        return switch (status) {
            case PENDING -> "Pending";
            case APPROVED -> "Approved";
            case REJECTED -> "Rejected";
        };
    }

    @Named("studentFromId")
    default Student studentFromId(UUID id, @Context StudentRepository studentRepository) {
        if (id == null) {
            return null;
        }
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + id + " during mapping"));
    }
    
    @Named("departmentFromId")
    default Department departmentFromId(UUID id, @Context DepartmentRepository departmentRepository) {
        if (id == null) {
            return null;
        }
        return departmentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Department not found with id: " + id + " during mapping"));
    }

    @Named("teacherFromId")
    default Teacher teacherFromId(UUID id, @Context TeacherRepository teacherRepository) {
        if (id == null) {
            return null;
        }
        return teacherRepository.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id: " + id + " during mapping"));
    }
}
