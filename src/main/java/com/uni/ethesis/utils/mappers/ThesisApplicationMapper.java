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
import com.uni.ethesis.exceptions.StudentNotFoundException;
import com.uni.ethesis.exceptions.TeacherNotFoundException;

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
