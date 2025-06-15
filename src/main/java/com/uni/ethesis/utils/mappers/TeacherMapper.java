package com.uni.ethesis.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.uni.ethesis.data.dto.TeacherDto;
import com.uni.ethesis.data.entities.Teacher;

@Mapper(componentModel = "spring")
public interface TeacherMapper {

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
}
