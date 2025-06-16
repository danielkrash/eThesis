package com.uni.ethesis.integration.service;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.uni.ethesis.data.entities.Department;
import com.uni.ethesis.data.repo.*;
import com.uni.ethesis.service.DepartmentService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.data.entities.Student;
import com.uni.ethesis.data.entities.Teacher;
import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.enums.TeacherPosition;
import com.uni.ethesis.enums.ThesisProposalStatus;
import com.uni.ethesis.exceptions.InvalidStatusTransitionException;
import com.uni.ethesis.exceptions.StudentNotFoundException;
import com.uni.ethesis.exceptions.TeacherNotFoundException;
import com.uni.ethesis.exceptions.ThesisProposalNotFoundException;
import com.uni.ethesis.service.ThesisProposalService;

@SpringBootTest
@Testcontainers
@Transactional
public class ThesisProposalServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:latest"
    );

    @Autowired
    private ThesisProposalService thesisProposalService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ThesisProposalRepository thesisProposalRepository; // For direct verification if needed

    private Student student;
    private Teacher teacher;
    private Department department;

    @BeforeEach
    void setUp() {

        thesisProposalRepository.deleteAll();
        studentRepository.deleteAll();
        teacherRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();

        // Create student with proper cascade
        User user1 = User.builder()
                .id(UUID.randomUUID())
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

        // Create teacher with proper cascade
        User user2 = User.builder()
                .id(UUID.randomUUID())
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

        department = Department.builder()
                .name("Test Department")
                .build();
        departmentRepository.save(department);
        departmentService.addUserToDepartment(department.getId(), student.getId());
        departmentService.addUserToDepartment(department.getId(), teacher.getId());
    }

    private ThesisProposalDto createSampleProposalDto(UUID studentId, UUID teacherId , UUID departmentId) {
        return ThesisProposalDto.builder()
                .title("Test Thesis Title")
                .goal("Test Goal")
                .objectives("Test Objectives")
                .technology("Test Tech")
                .studentId(studentId)
                .departmentId(departmentId)
                .teacherId(teacherId)
                .status(ThesisProposalStatus.PENDING)
                .build();
    }

    @Test
    void testCreateThesisProposal_Success() {
        ThesisProposalDto dto = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());

        ThesisProposalDto createdDto = thesisProposalService.createThesisProposal(dto);

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
        ThesisProposalDto dto = createSampleProposalDto(UUID.randomUUID(), teacher.getId() , department.getId()); // Non-existent studentId

        Exception exception = assertThrows(StudentNotFoundException.class, () -> {
            thesisProposalService.createThesisProposal(dto);
        });
        assertTrue(exception.getMessage().contains("Student not found"));
    }

    @Test
    void testCreateThesisProposal_TeacherNotFound() {
        ThesisProposalDto dto = createSampleProposalDto(student.getId(), UUID.randomUUID() , department.getId()); // Non-existent teacherId

        Exception exception = assertThrows(TeacherNotFoundException.class, () -> {
            thesisProposalService.createThesisProposal(dto);
        });
        assertTrue(exception.getMessage().contains("Teacher not found"));
    }

    @Test
    void testGetThesisProposalById_Success() {
        ThesisProposalDto initialDto = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        ThesisProposalDto createdDto = thesisProposalService.createThesisProposal(initialDto);

        ThesisProposalDto foundDto = thesisProposalService.getThesisProposalById(createdDto.getId());

        assertNotNull(foundDto);
        assertEquals(createdDto.getId(), foundDto.getId());
        assertEquals(initialDto.getTitle(), foundDto.getTitle());
        assertEquals(student.getId(), foundDto.getStudentId());
        assertEquals(teacher.getId(), foundDto.getTeacherId());
    }

    @Test
    void testUpdateThesisProposal_Success() {
        ThesisProposalDto initialDto = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        ThesisProposalDto createdDto = thesisProposalService.createThesisProposal(initialDto);

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

        ThesisProposalDto updatedResultDto = thesisProposalService.updateThesisProposal(createdDto.getId(), updateDto);

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
        ThesisProposalDto initialDto = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        ThesisProposalDto createdDto = thesisProposalService.createThesisProposal(initialDto);

        ThesisProposalDto updateDto = createSampleProposalDto(UUID.randomUUID(), teacher.getId() , department.getId()); // Invalid studentId
        updateDto.setTitle("Update with invalid student");

        Exception exception = assertThrows(StudentNotFoundException.class, () -> {
            thesisProposalService.updateThesisProposal(createdDto.getId(), updateDto);
        });
        assertTrue(exception.getMessage().contains("Student not found"));
    }

    @Test
    void testUpdateThesisProposal_TeacherNotFoundDuringUpdate() {
        ThesisProposalDto initialDto = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        ThesisProposalDto createdDto = thesisProposalService.createThesisProposal(initialDto);

        ThesisProposalDto updateDto = createSampleProposalDto(student.getId(), UUID.randomUUID() , department.getId()); // Invalid teacherId
        updateDto.setTitle("Update with invalid teacher");

        Exception exception = assertThrows(TeacherNotFoundException.class, () -> {
            thesisProposalService.updateThesisProposal(createdDto.getId(), updateDto);
        });
        assertTrue(exception.getMessage().contains("Teacher not found"));
    }

    @Test
    void testUpdateThesisProposalStatus_Success() {
        ThesisProposalDto initialDto = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        ThesisProposalDto createdDto = thesisProposalService.createThesisProposal(initialDto);

        ThesisProposalDto updatedStatusDto = thesisProposalService.updateThesisProposalStatus(createdDto.getId(), ThesisProposalStatus.REJECTED);

        assertNotNull(updatedStatusDto);
        assertEquals(ThesisProposalStatus.REJECTED, updatedStatusDto.getStatus());
        assertEquals(createdDto.getId(), updatedStatusDto.getId());
    }

    @Test
    void testGetAllThesisProposals_EmptyList() {
        List<ThesisProposalDto> proposals = thesisProposalService.getAllThesisProposals();
        assertNotNull(proposals);
        assertTrue(proposals.isEmpty());
    }

    @Test
    void testGetAllThesisProposals_WithProposals() {
        // Create multiple proposals
        ThesisProposalDto dto1 = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        dto1.setTitle("First Proposal");
        ThesisProposalDto dto2 = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        dto2.setTitle("Second Proposal");

        thesisProposalService.createThesisProposal(dto1);
        thesisProposalService.createThesisProposal(dto2);

        List<ThesisProposalDto> proposals = thesisProposalService.getAllThesisProposals();

        assertNotNull(proposals);
        assertEquals(2, proposals.size());
        assertTrue(proposals.stream().anyMatch(p -> "First Proposal".equals(p.getTitle())));
        assertTrue(proposals.stream().anyMatch(p -> "Second Proposal".equals(p.getTitle())));
    }

    @Test
    void testGetThesisProposalsByStudentId_EmptyList() {
        List<ThesisProposalDto> proposals = thesisProposalService.getThesisProposalsByStudentId(student.getId());
        assertNotNull(proposals);
        assertTrue(proposals.isEmpty());
    }

    @Test
    void testGetThesisProposalsByStudentId_WithProposals() {
        // Create proposals for the student
        ThesisProposalDto dto1 = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        dto1.setTitle("Student Proposal 1");
        ThesisProposalDto dto2 = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        dto2.setTitle("Student Proposal 2");

        thesisProposalService.createThesisProposal(dto1);
        thesisProposalService.createThesisProposal(dto2);

        List<ThesisProposalDto> proposals = thesisProposalService.getThesisProposalsByStudentId(student.getId());

        assertNotNull(proposals);
        assertEquals(2, proposals.size());
        assertTrue(proposals.stream().allMatch(p -> p.getStudentId().equals(student.getId())));
    }

    @Test
    void testGetThesisProposalsByStudentId_NonExistentStudent() {
        List<ThesisProposalDto> proposals = thesisProposalService.getThesisProposalsByStudentId(UUID.randomUUID());
        assertNotNull(proposals);
        assertTrue(proposals.isEmpty());
    }

    @Test
    void testGetThesisProposalsByTeacherId_EmptyList() {
        List<ThesisProposalDto> proposals = thesisProposalService.getThesisProposalsByTeacherId(teacher.getId());
        assertNotNull(proposals);
        assertTrue(proposals.isEmpty());
    }

    @Test
    void testGetThesisProposalsByTeacherId_WithProposals() {
        // Create proposals for the teacher
        ThesisProposalDto dto1 = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        dto1.setTitle("Teacher Proposal 1");
        ThesisProposalDto dto2 = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        dto2.setTitle("Teacher Proposal 2");

        thesisProposalService.createThesisProposal(dto1);
        thesisProposalService.createThesisProposal(dto2);

        List<ThesisProposalDto> proposals = thesisProposalService.getThesisProposalsByTeacherId(teacher.getId());

        assertNotNull(proposals);
        assertEquals(2, proposals.size());
        assertTrue(proposals.stream().allMatch(p -> p.getTeacherId().equals(teacher.getId())));
    }

    @Test
    void testGetThesisProposalsByTeacherId_NonExistentTeacher() {
        List<ThesisProposalDto> proposals = thesisProposalService.getThesisProposalsByTeacherId(UUID.randomUUID());
        assertNotNull(proposals);
        assertTrue(proposals.isEmpty());
    }

    @Test
    void testGetThesisProposalsByStatus_EmptyList() {
        List<ThesisProposalDto> proposals = thesisProposalService.getThesisProposalsByStatus(ThesisProposalStatus.APPROVED);
        assertNotNull(proposals);
        assertTrue(proposals.isEmpty());
    }

    @Test
    void testGetThesisProposalsByStatus_WithProposals() {
        // Create proposals with different statuses
        ThesisProposalDto dto1 = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        dto1.setTitle("Pending Proposal");
        ThesisProposalDto pendingProposal = thesisProposalService.createThesisProposal(dto1);

        ThesisProposalDto dto2 = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        dto2.setTitle("Another Pending Proposal");
        thesisProposalService.createThesisProposal(dto2);

        // Update one to approved status
        thesisProposalService.updateThesisProposalStatus(pendingProposal.getId(), ThesisProposalStatus.APPROVED);

        // Test pending status
        List<ThesisProposalDto> pendingProposals = thesisProposalService.getThesisProposalsByStatus(ThesisProposalStatus.PENDING);
        assertEquals(1, pendingProposals.size());
        assertEquals("Another Pending Proposal", pendingProposals.getFirst().getTitle());

        // Test approved status
        List<ThesisProposalDto> approvedProposals = thesisProposalService.getThesisProposalsByStatus(ThesisProposalStatus.APPROVED);
        assertEquals(1, approvedProposals.size());
        assertEquals("Pending Proposal", approvedProposals.getFirst().getTitle());

        // Test rejected status (should be empty)
        List<ThesisProposalDto> rejectedProposals = thesisProposalService.getThesisProposalsByStatus(ThesisProposalStatus.REJECTED);
        assertTrue(rejectedProposals.isEmpty());
    }

    @Test
    void testGetThesisProposalById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        
        Exception exception = assertThrows(ThesisProposalNotFoundException.class, () -> {
            thesisProposalService.getThesisProposalById(nonExistentId);
        });
        
        assertTrue(exception.getMessage().contains("Thesis proposal not found with id: " + nonExistentId));
    }

    @Test
    void testUpdateThesisProposal_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        ThesisProposalDto updateDto = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        updateDto.setTitle("Update Non-Existent");

        Exception exception = assertThrows(ThesisProposalNotFoundException.class, () -> {
            thesisProposalService.updateThesisProposal(nonExistentId, updateDto);
        });
        
        assertTrue(exception.getMessage().contains("Thesis proposal not found with id: " + nonExistentId));
    }

    @Test
    void testUpdateThesisProposalStatus_NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        Exception exception = assertThrows(ThesisProposalNotFoundException.class, () -> {
            thesisProposalService.updateThesisProposalStatus(nonExistentId, ThesisProposalStatus.APPROVED);
        });
        
        assertTrue(exception.getMessage().contains("Thesis proposal not found with id: " + nonExistentId));
    }

    @Test
    void testUpdateThesisProposalStatus_InvalidTransition_FromApproved() {
        ThesisProposalDto initialDto = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        ThesisProposalDto createdDto = thesisProposalService.createThesisProposal(initialDto);

        // First approve the proposal
        thesisProposalService.updateThesisProposalStatus(createdDto.getId(), ThesisProposalStatus.APPROVED);

        // Try to change from APPROVED to REJECTED (should fail)
        Exception exception = assertThrows(InvalidStatusTransitionException.class, () -> {
            thesisProposalService.updateThesisProposalStatus(createdDto.getId(), ThesisProposalStatus.REJECTED);
        });
        var message = exception.getMessage();
        assertTrue(exception.getMessage().contains("Cannot change thesis proposal status from APPROVED to REJECTED"));
    }

    @Test
    void testUpdateThesisProposalStatus_InvalidTransition_FromRejected() {
        ThesisProposalDto initialDto = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        ThesisProposalDto createdDto = thesisProposalService.createThesisProposal(initialDto);

        // First reject the proposal
        thesisProposalService.updateThesisProposalStatus(createdDto.getId(), ThesisProposalStatus.REJECTED);

        // Try to change from REJECTED to APPROVED (should fail)
        Exception exception = assertThrows(InvalidStatusTransitionException.class, () -> {
            thesisProposalService.updateThesisProposalStatus(createdDto.getId(), ThesisProposalStatus.APPROVED);
        });
        var message = exception.getMessage();
        assertTrue(exception.getMessage().contains("Cannot change thesis proposal status from REJECTED to APPROVED"));
    }

    @Test
    void testUpdateThesisProposalStatus_InvalidTargetStatus() {
        ThesisProposalDto initialDto = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        ThesisProposalDto createdDto = thesisProposalService.createThesisProposal(initialDto);

        // Try to change from PENDING to PENDING (should fail)
        Exception exception = assertThrows(InvalidStatusTransitionException.class, () -> {
            thesisProposalService.updateThesisProposalStatus(createdDto.getId(), ThesisProposalStatus.PENDING);
        });
        var message = exception.getMessage();
        assertTrue(exception.getMessage().contains("Invalid target status: PENDING"));
    }

    @Test
    void testDeleteThesisProposal_Success() {
        ThesisProposalDto initialDto = createSampleProposalDto(student.getId(), teacher.getId() , department.getId());
        ThesisProposalDto createdDto = thesisProposalService.createThesisProposal(initialDto);

        // Verify proposal exists before deletion
        assertNotNull(thesisProposalService.getThesisProposalById(createdDto.getId()));

        // Delete the proposal
        thesisProposalService.deleteThesisProposal(createdDto.getId());

        // Verify proposal no longer exists
        Exception exception = assertThrows(ThesisProposalNotFoundException.class, () -> {
            thesisProposalService.getThesisProposalById(createdDto.getId());
        });
        assertTrue(exception.getMessage().contains("Thesis proposal not found with id: " + createdDto.getId()));

        // Verify it's also not in repository
        assertFalse(thesisProposalRepository.findById(createdDto.getId()).isPresent());
    }

    @Test
    void testDeleteThesisProposal_NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        Exception exception = assertThrows(ThesisProposalNotFoundException.class, () -> {
            thesisProposalService.deleteThesisProposal(nonExistentId);
        });
        
        assertTrue(exception.getMessage().contains("Thesis proposal not found with id: " + nonExistentId));
    }
}
