package com.uni.ethesis.service;

import java.util.List;
import java.util.UUID;

import com.uni.ethesis.data.dto.StudentDto; // Added import for UUID

public interface StudentService {
    StudentDto createStudent(StudentDto studentDto);
    StudentDto getStudentById(UUID id); // Changed Long to UUID
    StudentDto getStudentByFacultyNumber(String facultyNumber);
    List<StudentDto> getAllStudents();
    StudentDto updateStudent(UUID id, StudentDto studentDto); // Changed Long to UUID
    void deleteStudent(UUID id); // Changed Long to UUID
    // TODO: Add methods for student-specific actions based on requirements
    // e.g., uploadThesis, viewApplicationStatus, viewApprovedTopics
}
