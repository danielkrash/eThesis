package com.uni.ethesis.unit.service;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uni.ethesis.data.dto.DefenseSessionDto;
import com.uni.ethesis.data.entities.Defense;
import com.uni.ethesis.data.entities.DefenseSession;
import com.uni.ethesis.data.entities.Thesis;
import com.uni.ethesis.data.repo.DefenseRepository;
import com.uni.ethesis.data.repo.DefenseSessionRepository;
import com.uni.ethesis.data.repo.ThesisRepository;
import com.uni.ethesis.enums.ThesisStatus;
import com.uni.ethesis.exceptions.DefenseNotFoundException;
import com.uni.ethesis.exceptions.DefenseSessionNotFoundException;
import com.uni.ethesis.exceptions.ThesisNotFoundException;
import com.uni.ethesis.service.impl.DefenseSessionServiceImpl;
import com.uni.ethesis.utils.mappers.DefenseSessionMapper;

@ExtendWith(MockitoExtension.class)
class DefenseSessionServiceTest {
    
    @Mock
    private DefenseSessionRepository defenseSessionRepository;
    
    @Mock
    private DefenseRepository defenseRepository;
    
    @Mock
    private ThesisRepository thesisRepository;
    
    @Mock
    private DefenseSessionMapper defenseSessionMapper;
    
    @InjectMocks
    private DefenseSessionServiceImpl defenseSessionService;
    
    private UUID defenseSessionId;
    private UUID defenseId;
    private UUID thesisId;
    private DefenseSession defenseSession;
    private Defense defense;
    private Thesis thesis;
    private DefenseSessionDto defenseSessionDto;
    private OffsetDateTime testDate;
    
    @BeforeEach
    void setUp() {
        defenseSessionId = UUID.randomUUID();
        defenseId = UUID.randomUUID();
        thesisId = UUID.randomUUID();
        testDate = OffsetDateTime.now();
        
        // Create test entities
        thesis = Thesis.builder()
                .id(thesisId)
                .status(ThesisStatus.READY_FOR_DEFENSE)
                .build();
                
        defense = Defense.builder()
                .id(defenseId)
                .build();
                
        defenseSession = DefenseSession.builder()
                .id(defenseSessionId)
                .dateAndTime(testDate)
                .notes("Test Notes")
                .thesis(thesis)
                .defense(defense)
                .build();
                
        defenseSessionDto = DefenseSessionDto.builder()
                .id(defenseSessionId)
                .dateAndTime(testDate)
                .notes("Test Notes")
                .thesisId(thesisId)
                .defenseId(defenseId)
                .build();
    }
    
    @Test
    void createDefenseSession_Success() {
        // Given
        when(defenseSessionMapper.defenseSessionDtoToDefenseSession(any(DefenseSessionDto.class))).thenReturn(defenseSession);
        when(thesisRepository.findById(thesisId)).thenReturn(Optional.of(thesis));
        when(defenseRepository.findById(defenseId)).thenReturn(Optional.of(defense));
        when(defenseSessionRepository.save(any(DefenseSession.class))).thenReturn(defenseSession);
        when(defenseSessionMapper.defenseSessionToDefenseSessionDto(any(DefenseSession.class))).thenReturn(defenseSessionDto);
        
        // When
        DefenseSessionDto result = defenseSessionService.createDefenseSession(defenseSessionDto);
        
        // Then
        assertNotNull(result);
        assertEquals(defenseSessionId, result.getId());
        assertEquals(testDate, result.getDateAndTime());
        assertEquals("Test Notes", result.getNotes());
        assertEquals(thesisId, result.getThesisId());
        assertEquals(defenseId, result.getDefenseId());
        
        verify(thesisRepository).findById(thesisId);
        verify(defenseRepository).findById(defenseId);
        verify(defenseSessionRepository).save(any(DefenseSession.class));
    }
    
    @Test
    void createDefenseSession_ThesisNotFound() {
        // Given
        when(defenseSessionMapper.defenseSessionDtoToDefenseSession(any(DefenseSessionDto.class))).thenReturn(defenseSession);
        when(thesisRepository.findById(thesisId)).thenReturn(Optional.empty());
        
        // When & Then
        Exception exception = assertThrows(ThesisNotFoundException.class, () -> {
            defenseSessionService.createDefenseSession(defenseSessionDto);
        });
        
        assertNotNull(exception);
        verify(thesisRepository).findById(thesisId);
        verify(defenseSessionRepository, never()).save(any(DefenseSession.class));
    }
    
    @Test
    void createDefenseSession_DefenseNotFound() {
        // Given
        when(defenseSessionMapper.defenseSessionDtoToDefenseSession(any(DefenseSessionDto.class))).thenReturn(defenseSession);
        when(thesisRepository.findById(thesisId)).thenReturn(Optional.of(thesis));
        when(defenseRepository.findById(defenseId)).thenReturn(Optional.empty());
        
        // When & Then
        Exception exception = assertThrows(DefenseNotFoundException.class, () -> {
            defenseSessionService.createDefenseSession(defenseSessionDto);
        });
        
        assertNotNull(exception);
        verify(thesisRepository).findById(thesisId);
        verify(defenseRepository).findById(defenseId);
        verify(defenseSessionRepository, never()).save(any(DefenseSession.class));
    }
    
    @Test
    void getDefenseSessionById_Success() {
        // Given
        when(defenseSessionRepository.findById(defenseSessionId)).thenReturn(Optional.of(defenseSession));
        when(defenseSessionMapper.defenseSessionToDefenseSessionDto(defenseSession)).thenReturn(defenseSessionDto);
        
        // When
        DefenseSessionDto result = defenseSessionService.getDefenseSessionById(defenseSessionId);
        
        // Then
        assertNotNull(result);
        assertEquals(defenseSessionId, result.getId());
        
        verify(defenseSessionRepository).findById(defenseSessionId);
    }
    
    @Test
    void getDefenseSessionById_NotFound() {
        // Given
        when(defenseSessionRepository.findById(defenseSessionId)).thenReturn(Optional.empty());
        
        // When & Then
        Exception exception = assertThrows(DefenseSessionNotFoundException.class, () -> {
            defenseSessionService.getDefenseSessionById(defenseSessionId);
        });
        
        assertNotNull(exception);
        verify(defenseSessionRepository).findById(defenseSessionId);
    }
    
    @Test
    void getAllDefenseSessions() {
        // Given
        List<DefenseSession> defenseSessions = Arrays.asList(defenseSession);
        when(defenseSessionRepository.findAll()).thenReturn(defenseSessions);
        when(defenseSessionMapper.defenseSessionToDefenseSessionDto(defenseSession)).thenReturn(defenseSessionDto);
        
        // When
        List<DefenseSessionDto> results = defenseSessionService.getAllDefenseSessions();
        
        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(defenseSessionId, results.get(0).getId());
        
        verify(defenseSessionRepository).findAll();
    }
    
    @Test
    void updateDefenseSession_Success() {
        // Given
        DefenseSessionDto updatedDto = DefenseSessionDto.builder()
                .id(defenseSessionId)
                .dateAndTime(testDate)
                .notes("Updated Notes")
                .thesisId(thesisId)
                .defenseId(defenseId)
                .build();
        
        // Mock the repository to return the session when findById is called
        when(defenseSessionRepository.findById(defenseSessionId)).thenReturn(Optional.of(defenseSession));
        
        // Mock the repository save method to return the session
        when(defenseSessionRepository.save(any(DefenseSession.class))).thenReturn(defenseSession);
        
        // Mock the mapper to return the DTO
        when(defenseSessionMapper.defenseSessionToDefenseSessionDto(any(DefenseSession.class))).thenReturn(updatedDto);
        
        // When
        DefenseSessionDto result = defenseSessionService.updateDefenseSession(defenseSessionId, updatedDto);
        
        // Then
        assertNotNull(result);
        
        // Verify that these methods were called
        verify(defenseSessionRepository).findById(defenseSessionId);
        verify(defenseSessionRepository).save(any(DefenseSession.class));
    }
    
    @Test
    void updateDefenseSession_NotFound() {
        // Given
        when(defenseSessionRepository.findById(defenseSessionId)).thenReturn(Optional.empty());
        
        // When & Then
        Exception exception = assertThrows(DefenseSessionNotFoundException.class, () -> {
            defenseSessionService.updateDefenseSession(defenseSessionId, defenseSessionDto);
        });
        
        assertNotNull(exception);
        verify(defenseSessionRepository).findById(defenseSessionId);
        verify(defenseSessionRepository, never()).save(any(DefenseSession.class));
    }
    
    @Test
    void deleteDefenseSession_Success() {
        // Given
        when(defenseSessionRepository.existsById(defenseSessionId)).thenReturn(true);
        doNothing().when(defenseSessionRepository).deleteById(defenseSessionId);
        
        // When
        defenseSessionService.deleteDefenseSession(defenseSessionId);
        
        // Then
        verify(defenseSessionRepository).existsById(defenseSessionId);
        verify(defenseSessionRepository).deleteById(defenseSessionId);
    }
    
    @Test
    void deleteDefenseSession_NotFound() {
        // Given
        when(defenseSessionRepository.existsById(defenseSessionId)).thenReturn(false);
        
        // When & Then
        Exception exception = assertThrows(DefenseSessionNotFoundException.class, () -> {
            defenseSessionService.deleteDefenseSession(defenseSessionId);
        });
        
        assertNotNull(exception);
        verify(defenseSessionRepository).existsById(defenseSessionId);
        verify(defenseSessionRepository, never()).deleteById(defenseSessionId);
    }
    
    @Test
    void getDefenseSessionsInPeriod() {
        // Given
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(1);
        OffsetDateTime endDate = OffsetDateTime.now().plusDays(1);
        
        when(defenseSessionRepository.findByDateAndTimeBetween(eq(startDate), eq(endDate)))
                .thenReturn(Collections.singletonList(defenseSession));
        when(defenseSessionMapper.defenseSessionToDefenseSessionDto(defenseSession)).thenReturn(defenseSessionDto);
        
        // When
        List<DefenseSessionDto> results = defenseSessionService.getDefenseSessionsInPeriod(startDate, endDate);
        
        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(defenseSessionDto, results.get(0));
        
        verify(defenseSessionRepository).findByDateAndTimeBetween(eq(startDate), eq(endDate));
    }
    
    @Test
    void getDefenseSessionByThesisId() {
        // Given
        when(defenseSessionRepository.findByThesisId(thesisId)).thenReturn(Optional.of(defenseSession));
        when(defenseSessionMapper.defenseSessionToDefenseSessionDto(defenseSession)).thenReturn(defenseSessionDto);
        
        // When
        DefenseSessionDto result = defenseSessionService.getDefenseSessionByThesisId(thesisId);
        
        // Then
        assertNotNull(result);
        assertEquals(defenseSessionId, result.getId());
        assertEquals(thesisId, result.getThesisId());
        
        verify(defenseSessionRepository).findByThesisId(thesisId);
    }
}
