package com.uni.ethesis.unit.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.uni.ethesis.data.dto.ThesisDto;
import com.uni.ethesis.data.entities.Student;
import com.uni.ethesis.data.entities.Teacher;
import com.uni.ethesis.data.entities.Thesis;
import com.uni.ethesis.data.entities.ThesisProposal;
import com.uni.ethesis.data.repo.ReviewRepository;
import com.uni.ethesis.data.repo.ThesisProposalRepository;
import com.uni.ethesis.data.repo.ThesisRepository;
import com.uni.ethesis.enums.ThesisStatus;
import com.uni.ethesis.exceptions.ThesisNotFoundException;
import com.uni.ethesis.exceptions.ThesisProposalNotFoundException;
import com.uni.ethesis.service.impl.ThesisServiceImpl;
import com.uni.ethesis.utils.mappers.ThesisMapper;

@ExtendWith(MockitoExtension.class)
class ThesisServiceTest {
    
    @Mock
    private ThesisRepository thesisRepository;
    
    @Mock
    private ThesisProposalRepository thesisProposalRepository;
    
    @Mock
    private ReviewRepository reviewRepository;
    
    @Mock
    private ThesisMapper thesisMapper;
    
    @Mock
    private MultipartFile mockPdfFile;
    
    @InjectMocks
    private ThesisServiceImpl thesisService;
    
    private UUID thesisId;
    private UUID proposalId;
    private UUID studentId;
    private UUID supervisorId;
    private Thesis thesis;
    private ThesisDto thesisDto;
    private ThesisProposal thesisProposal;
    private OffsetDateTime testDate;
    
    @BeforeEach
    void setUp() {
        thesisId = UUID.randomUUID();
        proposalId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        supervisorId = UUID.randomUUID();
        testDate = OffsetDateTime.now();
        
        // Create a Student entity
        Student student = Student.builder()
                .id(studentId)
                .build();
                
        // Create a Teacher entity
        Teacher teacher = Teacher.builder()
                .id(supervisorId)
                .build();
                
        thesisProposal = ThesisProposal.builder()
                .id(proposalId)
                .student(student)
                .teacher(teacher)
                .title("Test Proposal Title")
                .build();
        
        thesis = Thesis.builder()
                .id(thesisId)
                .pdfPath("test-thesis.pdf")
                .finalGrade(BigDecimal.valueOf(5.0))
                .status(ThesisStatus.READY_FOR_DEFENSE)
                .build();
                
        thesisDto = ThesisDto.builder()
                .id(thesisId)
                .pdfPath("test-thesis.pdf")
                .finalGrade(BigDecimal.valueOf(5.0))
                .status(ThesisStatus.READY_FOR_DEFENSE)
                .proposalId(proposalId)
                .build();
    }
    
    @Test
    void getThesisById_Success() {
        // Given
        when(thesisRepository.findById(thesisId)).thenReturn(Optional.of(thesis));
        when(thesisMapper.thesisToThesisDto(thesis)).thenReturn(thesisDto);
        
        // When
        ThesisDto result = thesisService.getThesisById(thesisId);
        
        // Then
        assertNotNull(result);
        assertEquals(thesisId, result.getId());
        assertEquals("test-thesis.pdf", result.getPdfPath());
        assertEquals(BigDecimal.valueOf(5.0), result.getFinalGrade());
        assertEquals(ThesisStatus.READY_FOR_DEFENSE, result.getStatus());
        assertEquals(proposalId, result.getProposalId());
        
        verify(thesisRepository).findById(thesisId);
        verify(thesisMapper).thesisToThesisDto(thesis);
    }
    
    @Test
    void getThesisById_NotFound() {
        // Given
        when(thesisRepository.findById(thesisId)).thenReturn(Optional.empty());
        
        // When & Then
        Exception exception = assertThrows(ThesisNotFoundException.class, () -> {
            thesisService.getThesisById(thesisId);
        });
        
        assertNotNull(exception);
        verify(thesisRepository).findById(thesisId);
        verify(thesisMapper, never()).thesisToThesisDto(any(Thesis.class));
    }
    
    @Test
    void createThesis_Success() {
        // Given
        when(thesisProposalRepository.findById(proposalId)).thenReturn(Optional.of(thesisProposal));
        when(thesisRepository.save(any(Thesis.class))).thenReturn(thesis);
        when(thesisMapper.thesisToThesisDto(thesis)).thenReturn(thesisDto);
        
        // When
        ThesisDto result = thesisService.createThesis(proposalId);
        
        // Then
        assertNotNull(result);
        assertEquals(thesisId, result.getId());
        assertEquals(proposalId, result.getProposalId());
        
        verify(thesisProposalRepository).findById(proposalId);
        verify(thesisRepository).save(any(Thesis.class));
    }
    
    @Test
    void createThesis_ProposalNotFound() {
        // Given
        when(thesisProposalRepository.findById(proposalId)).thenReturn(Optional.empty());
        
        // When & Then
        Exception exception = assertThrows(ThesisProposalNotFoundException.class, () -> {
            thesisService.createThesis(proposalId);
        });
        
        assertNotNull(exception);
        verify(thesisProposalRepository).findById(proposalId);
        verify(thesisRepository, never()).save(any(Thesis.class));
    }
    
    @Test
    void updateStatus_Success() {
        // Given
        ThesisStatus newStatus = ThesisStatus.DEFENDED;
        when(thesisRepository.findById(thesisId)).thenReturn(Optional.of(thesis));
        when(thesisRepository.save(any(Thesis.class))).thenReturn(thesis);
        when(thesisMapper.thesisToThesisDto(thesis)).thenReturn(thesisDto);
        
        // When
        ThesisDto result = thesisService.updateStatus(thesisId, newStatus);
        
        // Then
        assertNotNull(result);
        
        verify(thesisRepository).findById(thesisId);
        verify(thesisRepository).save(thesis);
    }
    
    @Test
    void updateStatus_NotFound() {
        // Given
        ThesisStatus newStatus = ThesisStatus.DEFENDED;
        when(thesisRepository.findById(thesisId)).thenReturn(Optional.empty());
        
        // When & Then
        Exception exception = assertThrows(ThesisNotFoundException.class, () -> {
            thesisService.updateStatus(thesisId, newStatus);
        });
        
        assertNotNull(exception);
        verify(thesisRepository).findById(thesisId);
        verify(thesisRepository, never()).save(any(Thesis.class));
    }
    
    @Test
    void updateGrade_Success() {
        // Given
        BigDecimal newGrade = BigDecimal.valueOf(4.5);
        when(thesisRepository.findById(thesisId)).thenReturn(Optional.of(thesis));
        when(thesisRepository.save(any(Thesis.class))).thenReturn(thesis);
        when(thesisMapper.thesisToThesisDto(thesis)).thenReturn(thesisDto);
        
        // When
        ThesisDto result = thesisService.updateGrade(thesisId, newGrade);
        
        // Then
        assertNotNull(result);
        
        verify(thesisRepository).findById(thesisId);
        verify(thesisRepository).save(thesis);
    }
    
    @Test
    void updateGrade_NotFound() {
        // Given
        BigDecimal newGrade = BigDecimal.valueOf(4.5);
        when(thesisRepository.findById(thesisId)).thenReturn(Optional.empty());
        
        // When & Then
        Exception exception = assertThrows(ThesisNotFoundException.class, () -> {
            thesisService.updateGrade(thesisId, newGrade);
        });
        
        assertNotNull(exception);
        verify(thesisRepository).findById(thesisId);
        verify(thesisRepository, never()).save(any(Thesis.class));
    }
    
    @Test
    void getAllTheses() {
        // Given
        List<Thesis> theses = Arrays.asList(thesis);
        when(thesisRepository.findAll()).thenReturn(theses);
        when(thesisMapper.thesisToThesisDto(thesis)).thenReturn(thesisDto);
        
        // When
        List<ThesisDto> results = thesisService.getAllTheses();
        
        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(thesisDto, results.get(0));
        
        verify(thesisRepository).findAll();
    }
    
    @Test
    void getThesesByStatus() {
        // Given
        ThesisStatus status = ThesisStatus.READY_FOR_DEFENSE;
        List<Thesis> theses = Arrays.asList(thesis);
        when(thesisRepository.findByStatus(status)).thenReturn(theses);
        when(thesisMapper.thesisToThesisDto(thesis)).thenReturn(thesisDto);
        
        // When
        List<ThesisDto> results = thesisService.getThesesByStatus(status);
        
        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(thesisDto, results.get(0));
        
        verify(thesisRepository).findByStatus(status);
    }
    
    @Test
    void deleteThesis_Success() {
        // Given
        when(thesisRepository.existsById(thesisId)).thenReturn(true);
        doNothing().when(thesisRepository).deleteById(thesisId);
        
        // When
        thesisService.deleteThesis(thesisId);
        
        // Then
        verify(thesisRepository).existsById(thesisId);
        verify(thesisRepository).deleteById(thesisId);
    }
    
    @Test
    void deleteThesis_NotFound() {
        // Given
        when(thesisRepository.existsById(thesisId)).thenReturn(false);
        
        // When & Then
        Exception exception = assertThrows(ThesisNotFoundException.class, () -> {
            thesisService.deleteThesis(thesisId);
        });
        
        assertNotNull(exception);
        verify(thesisRepository).existsById(thesisId);
        verify(thesisRepository, never()).deleteById(any(UUID.class));
    }
}
