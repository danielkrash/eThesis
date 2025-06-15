package com.uni.ethesis.utils.mappers;

import java.util.UUID;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.data.entities.Student;
import com.uni.ethesis.data.entities.Teacher;
import com.uni.ethesis.data.entities.ThesisProposal;
import com.uni.ethesis.data.repo.StudentRepository;
import com.uni.ethesis.data.repo.TeacherRepository;
import com.uni.ethesis.enums.ThesisProposalStatus;
import com.uni.ethesis.exceptions.StudentNotFoundException;
import com.uni.ethesis.exceptions.TeacherNotFoundException;
import com.uni.ethesis.web.view.model.ThesisProposalViewModel;

@Mapper(componentModel = "spring", uses = {StudentRepository.class, TeacherRepository.class})
public interface ThesisApplicationMapper {

    ThesisApplicationMapper INSTANCE = Mappers.getMapper(ThesisApplicationMapper.class);

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "teacher.id", target = "teacherId")
    ThesisProposalDto thesisProposalToThesisProposalDto(ThesisProposal thesisProposal);

    @Mapping(source = "studentId", target = "student", qualifiedByName = "studentFromId")
    @Mapping(source = "teacherId", target = "teacher", qualifiedByName = "teacherFromId")
    @Mapping(target = "thesis", ignore = true) // Thesis is created later, not from DTO
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    ThesisProposal thesisProposalDtoToThesisProposal(ThesisProposalDto thesisProposalDto, @Context StudentRepository studentRepository, @Context TeacherRepository teacherRepository);

    @Mapping(source = "studentId", target = "student", qualifiedByName = "studentFromId")
    @Mapping(source = "teacherId", target = "teacher", qualifiedByName = "teacherFromId")
    @Mapping(target = "thesis", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateThesisProposalFromDto(ThesisProposalDto thesisProposalDto, @MappingTarget ThesisProposal thesisProposal, @Context StudentRepository studentRepository, @Context TeacherRepository teacherRepository);

    // View Model mapping methods using MapStruct
    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    @Mapping(source = "studentId", target = "studentId", qualifiedByName = "uuidToString")
    @Mapping(source = "teacherId", target = "teacherId", qualifiedByName = "uuidToString")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    @Mapping(source = "status", target = "statusDisplayName", qualifiedByName = "statusToDisplayName")
    @Mapping(target = "studentName", ignore = true)
    @Mapping(target = "teacherName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "canEdit", ignore = true)
    @Mapping(target = "canApply", ignore = true)
    @Mapping(target = "canApprove", ignore = true)
    @Mapping(target = "canReject", ignore = true)
    ThesisProposalViewModel toViewModel(ThesisProposalDto dto);

    @Mapping(source = "id", target = "id", qualifiedByName = "stringToUuid")
    @Mapping(source = "studentId", target = "studentId", qualifiedByName = "stringToUuid")
    @Mapping(source = "teacherId", target = "teacherId", qualifiedByName = "stringToUuid")
    @Mapping(source = "status", target = "status", qualifiedByName = "stringToStatus")
    ThesisProposalDto toDto(ThesisProposalViewModel viewModel);

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

    @Named("teacherFromId")
    default Teacher teacherFromId(UUID id, @Context TeacherRepository teacherRepository) {
        if (id == null) {
            return null;
        }
        return teacherRepository.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id: " + id + " during mapping"));
    }
}
