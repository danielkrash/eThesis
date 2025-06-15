package com.uni.ethesis.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uni.ethesis.data.dto.TeacherDto;
import com.uni.ethesis.data.entities.Student;
import com.uni.ethesis.data.entities.Teacher;
import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.data.repo.StudentRepository;
import com.uni.ethesis.data.repo.TeacherRepository;
import com.uni.ethesis.data.repo.UserRepository;
import com.uni.ethesis.exceptions.TeacherNotFoundException;
import com.uni.ethesis.service.TeacherService;
import com.uni.ethesis.utils.mappers.TeacherMapper;

@Service
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherMapper teacherMapper;

    @Autowired
    public TeacherServiceImpl(TeacherRepository teacherRepository,
                              UserRepository userRepository,
                              StudentRepository studentRepository,
                              TeacherMapper teacherMapper) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherMapper = teacherMapper;
    }

    @Override
    @Transactional
    public TeacherDto createTeacher(TeacherDto teacherDto) {
        User userToSave = User.builder()
                .firstName(teacherDto.getFirstName())
                .lastName(teacherDto.getLastName())
                .email(teacherDto.getEmail())
                .build();
        User savedUser = userRepository.save(userToSave);

        Teacher teacherToSave = Teacher.builder()
                .user(savedUser)
                .id(savedUser.getId())
                .position(teacherDto.getPosition())
                .build();
        Teacher savedTeacher = teacherRepository.save(teacherToSave);
        return teacherMapper.teacherToTeacherDto(savedTeacher);
    }

    @Override
    public TeacherDto getTeacherById(UUID id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id: " + id));
        return teacherMapper.teacherToTeacherDto(teacher);
    }

    @Override
    public TeacherDto getTeacherByEmail(String email) {
        Teacher teacher = teacherRepository.findByUserEmailIgnoreCase(email);
        if (teacher == null) {
            throw new TeacherNotFoundException("Teacher not found with email: " + email);
        }
        return teacherMapper.teacherToTeacherDto(teacher);
    }

    @Override
    public List<TeacherDto> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(teacherMapper::teacherToTeacherDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TeacherDto updateTeacher(UUID id, TeacherDto teacherDto) {
        return teacherRepository.findById(id)
                .map(teacher -> {
                    User user = teacher.getUser();
                    if (user == null) {
                        throw new IllegalStateException("User data is missing for teacher id: " + id);
                    }

                    user.setFirstName(teacherDto.getFirstName() == null ? user.getFirstName() : teacherDto.getFirstName());
                    user.setLastName(teacherDto.getLastName() == null ? user.getLastName() : teacherDto.getLastName());
                    user.setEmail(teacherDto.getEmail() == null ? user.getEmail() : teacherDto.getEmail());
                    userRepository.save(user);

                    teacher.setPosition(teacherDto.getPosition() == null ? teacher.getPosition() : teacherDto.getPosition());
                    Teacher updatedTeacher = teacherRepository.save(teacher);
                    return teacherMapper.teacherToTeacherDto(updatedTeacher);
                })
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteTeacher(UUID id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id: " + id));
        
        User user = teacher.getUser();
        teacherRepository.delete(teacher);

        if (user != null) {
            // Check if user has a student role before deleting
            Optional<Student> student = studentRepository.findByUser(user);
            if (student.isEmpty()) { 
                 userRepository.delete(user);
            }
        }
    }

    @Override
    @Transactional
    public TeacherDto promoteUserToTeacher(UUID userId, TeacherDto teacherDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.uni.ethesis.exceptions.UserNotFoundException("User not found with id: " + userId));

        if (teacherRepository.findById(userId).isPresent()) {
            throw new IllegalStateException("User is already a teacher.");
        }

        Teacher teacherToSave = Teacher.builder()
                .user(user)
                .id(user.getId())
                .position(teacherDto.getPosition())
                .build();
        Teacher savedTeacher = teacherRepository.save(teacherToSave);
        return teacherMapper.teacherToTeacherDto(savedTeacher);
    }
}
