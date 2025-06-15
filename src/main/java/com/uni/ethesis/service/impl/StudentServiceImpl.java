package com.uni.ethesis.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uni.ethesis.data.dto.StudentDto;
import com.uni.ethesis.data.entities.Student;
import com.uni.ethesis.data.entities.Teacher;
import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.data.repo.StudentRepository;
import com.uni.ethesis.data.repo.TeacherRepository;
import com.uni.ethesis.data.repo.UserRepository;
import com.uni.ethesis.exceptions.StudentNotFoundException;
import com.uni.ethesis.service.StudentService;
import com.uni.ethesis.utils.mappers.StudentMapper;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentMapper studentMapper;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository,
                              UserRepository userRepository,
                              TeacherRepository teacherRepository,
                              StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.studentMapper = studentMapper;
    }

    @Override
    @Transactional
    public StudentDto createStudent(StudentDto studentDto) {
        User userToSave = User.builder()
                .firstName(studentDto.getFirstName())
                .lastName(studentDto.getLastName())
                .build();
        User savedUser = userRepository.save(userToSave);

        Student studentToSave = Student.builder()
                .user(savedUser)
                .id(savedUser.getId())
                .universityId(studentDto.getUniversityId())
                .build();
        Student savedStudent = studentRepository.save(studentToSave);
        return studentMapper.studentToStudentDto(savedStudent);
    }

    @Override
    public StudentDto getStudentById(UUID id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + id));
        return studentMapper.studentToStudentDto(student);
    }

    @Override
    public StudentDto getStudentByFacultyNumber(String facultyNumber) {
        Student student = studentRepository.findByUniversityIdIgnoreCase(facultyNumber);
        if (student == null) {
            throw new StudentNotFoundException("Student not found with faculty number: " + facultyNumber);
        }
        return studentMapper.studentToStudentDto(student);
    }

    @Override
    public List<StudentDto> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(studentMapper::studentToStudentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StudentDto updateStudent(UUID id, StudentDto studentDto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + id));

        User user = student.getUser();
        if (user == null) {
            throw new IllegalStateException("User data is missing for student id: " + id);
        }
        user.setFirstName(studentDto.getFirstName());
        user.setLastName(studentDto.getLastName());
        userRepository.save(user);

        student.setUniversityId(studentDto.getUniversityId());
        Student updatedStudent = studentRepository.save(student);
        return studentMapper.studentToStudentDto(updatedStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(UUID id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + id));
        
        User user = student.getUser();
        studentRepository.delete(student);

        if (user != null) {
            // Check if user has a teacher role before deleting
            Teacher teacher = teacherRepository.findByUser(user);
            if (teacher == null) { 
                 userRepository.delete(user);
            }
        }
    }
}
