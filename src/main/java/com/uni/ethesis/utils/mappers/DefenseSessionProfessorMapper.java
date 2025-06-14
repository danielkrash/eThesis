package com.uni.ethesis.utils.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.uni.ethesis.data.dto.DefenseSessionProfessorDto;
import com.uni.ethesis.data.entities.DefenseSessionProfessor;

@Mapper(componentModel = "spring")
public interface DefenseSessionProfessorMapper {

    DefenseSessionProfessorMapper INSTANCE = Mappers.getMapper(DefenseSessionProfessorMapper.class);

    @Mapping(source = "defenseSession.id", target = "defenseSessionId")
    @Mapping(source = "professor.id", target = "professorId")
    @Mapping(source = "professor", target = "professorFullName", qualifiedByName = "mapProfessorFullName")
    @Mapping(source = "professor.position", target = "professorPosition")
    @Mapping(source = "defenseSession.dateAndTime", target = "defenseSessionDateTime", qualifiedByName = "mapOffsetDateTimeToLocalDateTime")
    @Mapping(source = "defenseSession.thesis.proposal.title", target = "thesisTitle")
    @Mapping(source = "defenseSession.thesis.proposal.student.user", target = "studentName", qualifiedByName = "mapStudentName")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "mapOffsetDateTimeToLocalDateTime")
    @Mapping(source = "lastModifiedAt", target = "lastModifiedAt", qualifiedByName = "mapOffsetDateTimeToLocalDateTime")
    DefenseSessionProfessorDto defenseSessionProfessorToDto(DefenseSessionProfessor defenseSessionProfessor);

    @Mapping(target = "defenseSession", ignore = true) // Will be set manually in service
    @Mapping(target = "professor", ignore = true) // Will be set manually in service
    @Mapping(target = "id", ignore = true) // Will be set manually in service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    DefenseSessionProfessor dtoToDefenseSessionProfessor(DefenseSessionProfessorDto dto);

    List<DefenseSessionProfessorDto> defenseSessionProfessorsToDto(List<DefenseSessionProfessor> defenseSessionProfessors);

    @Named("mapProfessorFullName")
    default String mapProfessorFullName(com.uni.ethesis.data.entities.Teacher professor) {
        if (professor == null || professor.getUser() == null) {
            return null;
        }
        return professor.getUser().getFirstName() + " " + professor.getUser().getLastName();
    }

    @Named("mapStudentName")
    default String mapStudentName(com.uni.ethesis.data.entities.User user) {
        if (user == null) {
            return null;
        }
        return user.getFirstName() + " " + user.getLastName();
    }

    @Named("mapOffsetDateTimeToLocalDateTime")
    default java.time.LocalDateTime mapOffsetDateTimeToLocalDateTime(java.time.OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }
}
