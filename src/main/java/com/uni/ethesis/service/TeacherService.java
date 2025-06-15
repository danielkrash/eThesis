package com.uni.ethesis.service;

import java.util.List;
import java.util.UUID;

import com.uni.ethesis.data.dto.TeacherDto;

public interface TeacherService {
    TeacherDto createTeacher(TeacherDto teacherDto);
    TeacherDto getTeacherById(UUID id);
    TeacherDto getTeacherByEmail(String email);
    List<TeacherDto> getAllTeachers();
    TeacherDto updateTeacher(UUID id, TeacherDto teacherDto);
    void deleteTeacher(UUID id);
    TeacherDto promoteUserToTeacher(UUID userId, TeacherDto teacherDto);
    // TODO: Add methods for teacher-specific actions like submitting thesis applications, reviewing theses
}
