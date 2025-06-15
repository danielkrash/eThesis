package com.uni.ethesis.unit.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uni.ethesis.data.dto.DefenseSessionProfessorDto;
import com.uni.ethesis.data.entities.DefenseSession;
import com.uni.ethesis.data.entities.DefenseSessionProfessor;
import com.uni.ethesis.data.entities.Teacher;
import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.data.repo.DefenseSessionProfessorRepository;
import com.uni.ethesis.data.repo.DefenseSessionRepository;
import com.uni.ethesis.data.repo.TeacherRepository;
import com.uni.ethesis.exceptions.DefenseSessionNotFoundException;
import com.uni.ethesis.exceptions.ProfessorAlreadyAssignedException;
import com.uni.ethesis.exceptions.TeacherNotFoundException;
import com.uni.ethesis.service.impl.DefenseSessionProfessorServiceImpl;
import com.uni.ethesis.utils.DefenseSessionProfessorKey;
import com.uni.ethesis.utils.mappers.DefenseSessionProfessorMapper;

@ExtendWith(MockitoExtension.class)
class DefenseSessionProfessorServiceTest {

    @Mock
    private DefenseSessionProfessorRepository defenseSessionProfessorRepository;

    @Mock
    private DefenseSessionRepository defenseSessionRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private DefenseSessionProfessorMapper defenseSessionProfessorMapper;

    @InjectMocks
    private DefenseSessionProfessorServiceImpl defenseSessionProfessorService;

    private UUID defenseSessionId;
    private UUID professorId;
    private DefenseSession defenseSession;
    private Teacher professor;
    private User professorUser;
    private DefenseSessionProfessor defenseSessionProfessor;
    private DefenseSessionProfessorDto defenseSessionProfessorDto;
    private DefenseSessionProfessorKey key;

    @BeforeEach
    void setUp() {
        defenseSessionId = UUID.randomUUID();
        professorId = UUID.randomUUID();

        professorUser = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Professor")
                .email("john.professor@example.com")
                .build();

        professor = Teacher.builder()
                .id(professorId)
                .user(professorUser)
                .build();

        defenseSession = DefenseSession.builder()
                .id(defenseSessionId)
                .notes("Test defense session")
                .build();

        key = new DefenseSessionProfessorKey(defenseSessionId, professorId);

        defenseSessionProfessor = DefenseSessionProfessor.builder()
                .id(key)
                .defenseSession(defenseSession)
                .professor(professor)
                .grade(null)
                .thoughts(null)
                .build();

        defenseSessionProfessorDto = DefenseSessionProfessorDto.builder()
                .defenseSessionId(defenseSessionId)
                .professorId(professorId)
                .professorFullName("John Professor")
                .grade(null)
                .thoughts(null)
                .build();
    }

    @Test
    void addProfessorToDefenseSession_Success() {
        // Given
        when(defenseSessionRepository.findById(defenseSessionId)).thenReturn(Optional.of(defenseSession));
        when(teacherRepository.findById(professorId)).thenReturn(Optional.of(professor));
        when(defenseSessionProfessorRepository.existsByDefenseSessionIdAndProfessorId(defenseSessionId, professorId))
                .thenReturn(false);
        when(defenseSessionProfessorRepository.save(any(DefenseSessionProfessor.class))).thenReturn(defenseSessionProfessor);
        when(defenseSessionProfessorMapper.defenseSessionProfessorToDto(defenseSessionProfessor))
                .thenReturn(defenseSessionProfessorDto);

        // When
        DefenseSessionProfessorDto result = defenseSessionProfessorService.addProfessorToDefenseSession(defenseSessionId, professorId);

        // Then
        assertNotNull(result);
        assertEquals(defenseSessionId, result.getDefenseSessionId());
        assertEquals(professorId, result.getProfessorId());
        assertEquals("John Professor", result.getProfessorFullName());

        verify(defenseSessionProfessorRepository).save(any(DefenseSessionProfessor.class));
    }

    @Test
    void addProfessorToDefenseSession_DefenseSessionNotFound() {
        // Given
        when(defenseSessionRepository.findById(defenseSessionId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DefenseSessionNotFoundException.class,
                () -> defenseSessionProfessorService.addProfessorToDefenseSession(defenseSessionId, professorId));
    }

    @Test
    void addProfessorToDefenseSession_TeacherNotFound() {
        // Given
        when(defenseSessionRepository.findById(defenseSessionId)).thenReturn(Optional.of(defenseSession));
        when(teacherRepository.findById(professorId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TeacherNotFoundException.class,
                () -> defenseSessionProfessorService.addProfessorToDefenseSession(defenseSessionId, professorId));
    }

    @Test
    void addProfessorToDefenseSession_ProfessorAlreadyAssigned() {
        // Given
        when(defenseSessionRepository.findById(defenseSessionId)).thenReturn(Optional.of(defenseSession));
        when(teacherRepository.findById(professorId)).thenReturn(Optional.of(professor));
        when(defenseSessionProfessorRepository.existsByDefenseSessionIdAndProfessorId(defenseSessionId, professorId))
                .thenReturn(true);

        // When & Then
        assertThrows(ProfessorAlreadyAssignedException.class,
                () -> defenseSessionProfessorService.addProfessorToDefenseSession(defenseSessionId, professorId));
    }

    @Test
    void updateProfessorEvaluation_Success() {
        // Given
        Integer grade = 85;
        String thoughts = "Good presentation and solid understanding of the topic.";
        
        DefenseSessionProfessor updatedDefenseSessionProfessor = DefenseSessionProfessor.builder()
                .id(key)
                .defenseSession(defenseSession)
                .professor(professor)
                .grade(grade)
                .thoughts(thoughts)
                .build();

        DefenseSessionProfessorDto updatedDto = DefenseSessionProfessorDto.builder()
                .defenseSessionId(defenseSessionId)
                .professorId(professorId)
                .grade(grade)
                .thoughts(thoughts)
                .build();

        when(defenseSessionProfessorRepository.findByDefenseSessionIdAndProfessorId(defenseSessionId, professorId))
                .thenReturn(Optional.of(defenseSessionProfessor));
        when(defenseSessionProfessorRepository.save(any(DefenseSessionProfessor.class)))
                .thenReturn(updatedDefenseSessionProfessor);
        when(defenseSessionProfessorMapper.defenseSessionProfessorToDto(updatedDefenseSessionProfessor))
                .thenReturn(updatedDto);

        // When
        DefenseSessionProfessorDto result = defenseSessionProfessorService.updateProfessorEvaluation(
                defenseSessionId, professorId, grade, thoughts);

        // Then
        assertNotNull(result);
        assertEquals(grade, result.getGrade());
        assertEquals(thoughts, result.getThoughts());

        verify(defenseSessionProfessorRepository).save(any(DefenseSessionProfessor.class));
    }

    @Test
    void getProfessorsByDefenseSession_Success() {
        // Given
        List<DefenseSessionProfessor> professors = Arrays.asList(defenseSessionProfessor);
        List<DefenseSessionProfessorDto> professorDtos = Arrays.asList(defenseSessionProfessorDto);

        when(defenseSessionRepository.existsById(defenseSessionId)).thenReturn(true);
        when(defenseSessionProfessorRepository.findByDefenseSessionId(defenseSessionId)).thenReturn(professors);
        when(defenseSessionProfessorMapper.defenseSessionProfessorsToDto(professors)).thenReturn(professorDtos);

        // When
        List<DefenseSessionProfessorDto> result = defenseSessionProfessorService.getProfessorsByDefenseSession(defenseSessionId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(defenseSessionId, result.get(0).getDefenseSessionId());
    }

    @Test
    void isProfessorAssignedToDefenseSession_True() {
        // Given
        when(defenseSessionProfessorRepository.existsByDefenseSessionIdAndProfessorId(defenseSessionId, professorId))
                .thenReturn(true);

        // When
        boolean result = defenseSessionProfessorService.isProfessorAssignedToDefenseSession(defenseSessionId, professorId);

        // Then
        assertTrue(result);
    }

    @Test
    void isProfessorAssignedToDefenseSession_False() {
        // Given
        when(defenseSessionProfessorRepository.existsByDefenseSessionIdAndProfessorId(defenseSessionId, professorId))
                .thenReturn(false);

        // When
        boolean result = defenseSessionProfessorService.isProfessorAssignedToDefenseSession(defenseSessionId, professorId);

        // Then
        assertFalse(result);
    }

    @Test
    void getAverageGradeForDefenseSession_Success() {
        // Given
        Double expectedAverage = 87.5;
        when(defenseSessionRepository.existsById(defenseSessionId)).thenReturn(true);
        when(defenseSessionProfessorRepository.getAverageGradeByDefenseSessionId(defenseSessionId))
                .thenReturn(expectedAverage);

        // When
        Double result = defenseSessionProfessorService.getAverageGradeForDefenseSession(defenseSessionId);

        // Then
        assertEquals(expectedAverage, result);
    }
}
