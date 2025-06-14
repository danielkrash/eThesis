package com.uni.ethesis.integration.service;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.data.entities.Student;
import com.uni.ethesis.data.entities.Teacher;
import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.data.repo.StudentRepository;
import com.uni.ethesis.data.repo.TeacherRepository;
import com.uni.ethesis.data.repo.ThesisProposalRepository;
import com.uni.ethesis.data.repo.UserRepository;
import com.uni.ethesis.enums.TeacherPosition;
import com.uni.ethesis.enums.ThesisProposalStatus;
import com.uni.ethesis.exceptions.StudentNotFoundException;
import com.uni.ethesis.exceptions.TeacherNotFoundException;
import com.uni.ethesis.service.ThesisApplicationService;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Testcontainers
@Transactional
public class ThesisApplicationServiceIntegrationTest{

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:latest"
    );

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ThesisApplicationService thesisApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ThesisProposalRepository thesisProposalRepository; // For direct verification if needed

    private User studentUser, teacherUser;
    private Student student;
    private Teacher teacher;

    @BeforeEach
    void setUp() {
        // Clear any existing data to prevent conflicts
        thesisProposalRepository.deleteAll();
        studentRepository.deleteAll();
        teacherRepository.deleteAll();
        userRepository.deleteAll();

        // Create student with proper cascade
        User user1 = User.builder()
                .email("student@example.com")
                .firstName("Student")
                .lastName("User")
                .build();
        
        Student student1 = Student.builder()
                .user(user1)
                .universityId("f106503")
                .build();
        
        // Save student (will cascade to user due to CascadeType.PERSIST/MERGE)
        student = studentRepository.save(student1);
        studentUser = student.getUser();

        // Create teacher with proper cascade
        User user2 = User.builder()
                .email("teacher@example.com")
                .firstName("Teacher")
                .lastName("User")
                .build();
        
        Teacher teacher1 = Teacher.builder()
                .user(user2)
                .position(TeacherPosition.PROFESSOR)
                .build();
        
        // Save teacher (will cascade to user due to CascadeType.PERSIST/MERGE)
        teacher = teacherRepository.save(teacher1);
        teacherUser = teacher.getUser();
    }

    private ThesisProposalDto createSampleProposalDto(UUID studentId, UUID teacherId) {
        return ThesisProposalDto.builder()
                .title("Test Thesis Title")
                .goal("Test Goal")
                .objectives("Test Objectives")
                .technology("Test Tech")
                .studentId(studentId)
                .teacherId(teacherId)
                .status(ThesisProposalStatus.PENDING)
                .build();
    }

    @Test
    void testCreateThesisProposal_Success() {
        ThesisProposalDto dto = createSampleProposalDto(student.getId(), teacher.getId());

        ThesisProposalDto createdDto = thesisApplicationService.createThesisProposal(dto);

        assertNotNull(createdDto);
        assertNotNull(createdDto.getId());
        assertEquals(dto.getTitle(), createdDto.getTitle());
        assertEquals(dto.getStudentId(), createdDto.getStudentId());
        assertEquals(dto.getTeacherId(), createdDto.getTeacherId());
        assertEquals(dto.getStatus(), createdDto.getStatus());

        // Verify underlying entity and mapper's work
        assertTrue(thesisProposalRepository.findById(createdDto.getId()).isPresent());
        assertEquals(student.getId(), thesisProposalRepository.findById(createdDto.getId()).get().getStudent().getId());
        assertEquals(teacher.getId(), thesisProposalRepository.findById(createdDto.getId()).get().getTeacher().getId());
    }

    @Test
    void testCreateThesisProposal_StudentNotFound() {
        ThesisProposalDto dto = createSampleProposalDto(UUID.randomUUID(), teacher.getId()); // Non-existent studentId

        Exception exception = assertThrows(StudentNotFoundException.class, () -> {
            thesisApplicationService.createThesisProposal(dto);
        });
        assertTrue(exception.getMessage().contains("Student not found"));
    }

    @Test
    void testCreateThesisProposal_TeacherNotFound() {
        ThesisProposalDto dto = createSampleProposalDto(student.getId(), UUID.randomUUID()); // Non-existent teacherId

        Exception exception = assertThrows(TeacherNotFoundException.class, () -> {
            thesisApplicationService.createThesisProposal(dto);
        });
        assertTrue(exception.getMessage().contains("Teacher not found"));
    }

    @Test
    void testGetThesisProposalById_Success() {
        ThesisProposalDto initialDto = createSampleProposalDto(student.getId(), teacher.getId());
        ThesisProposalDto createdDto = thesisApplicationService.createThesisProposal(initialDto);

        ThesisProposalDto foundDto = thesisApplicationService.getThesisProposalById(createdDto.getId());

        assertNotNull(foundDto);
        assertEquals(createdDto.getId(), foundDto.getId());
        assertEquals(initialDto.getTitle(), foundDto.getTitle());
        assertEquals(student.getId(), foundDto.getStudentId());
        assertEquals(teacher.getId(), foundDto.getTeacherId());
    }

    @Test
    void testUpdateThesisProposal_Success() {
        ThesisProposalDto initialDto = createSampleProposalDto(student.getId(), teacher.getId());
        ThesisProposalDto createdDto = thesisApplicationService.createThesisProposal(initialDto);

        ThesisProposalDto updateDto = ThesisProposalDto.builder()
                .id(createdDto.getId()) // Not strictly needed for update DTO, but good for clarity
                .title("Updated Test Title")
                .goal("Updated Goal")
                .objectives(createdDto.getObjectives()) // Keep some fields same
                .technology("Updated Tech")
                .studentId(student.getId()) // Mapper should handle these correctly
                .teacherId(teacher.getId())
                .status(ThesisProposalStatus.APPROVED)
                .build();

        ThesisProposalDto updatedResultDto = thesisApplicationService.updateThesisProposal(createdDto.getId(), updateDto);

        assertNotNull(updatedResultDto);
        assertEquals(createdDto.getId(), updatedResultDto.getId());
        assertEquals("Updated Test Title", updatedResultDto.getTitle());
        assertEquals(ThesisProposalStatus.APPROVED, updatedResultDto.getStatus());
        assertEquals(student.getId(), updatedResultDto.getStudentId());
        assertEquals(teacher.getId(), updatedResultDto.getTeacherId());

        // Verify underlying entity
        assertTrue(thesisProposalRepository.findById(createdDto.getId()).isPresent());
        assertEquals("Updated Test Title", thesisProposalRepository.findById(createdDto.getId()).get().getTitle());
    }

    @Test
    void testUpdateThesisProposal_StudentNotFoundDuringUpdate() {
        ThesisProposalDto initialDto = createSampleProposalDto(student.getId(), teacher.getId());
        ThesisProposalDto createdDto = thesisApplicationService.createThesisProposal(initialDto);

        ThesisProposalDto updateDto = createSampleProposalDto(UUID.randomUUID(), teacher.getId()); // Invalid studentId
        updateDto.setTitle("Update with invalid student");

        Exception exception = assertThrows(StudentNotFoundException.class, () -> {
            thesisApplicationService.updateThesisProposal(createdDto.getId(), updateDto);
        });
        assertTrue(exception.getMessage().contains("Student not found"));
    }

     @Test
    void testUpdateThesisProposalStatus_Success() {
        ThesisProposalDto initialDto = createSampleProposalDto(student.getId(), teacher.getId());
        ThesisProposalDto createdDto = thesisApplicationService.createThesisProposal(initialDto);

        ThesisProposalDto updatedStatusDto = thesisApplicationService.updateThesisProposalStatus(createdDto.getId(), ThesisProposalStatus.REJECTED);

        assertNotNull(updatedStatusDto);
        assertEquals(ThesisProposalStatus.REJECTED, updatedStatusDto.getStatus());
        assertEquals(createdDto.getId(), updatedStatusDto.getId());
    }
}
