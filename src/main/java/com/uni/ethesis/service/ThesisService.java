package com.uni.ethesis.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.uni.ethesis.data.dto.ThesisDto;
import com.uni.ethesis.enums.ThesisStatus;

public interface ThesisService {
    
    // Basic CRUD operations
    ThesisDto createThesis(UUID proposalId);
    ThesisDto getThesisById(UUID id);
    ThesisDto getThesisByProposalId(UUID proposalId);
    ThesisDto getThesisByStudentId(UUID studentId);
    List<ThesisDto> getAllTheses();
    ThesisDto updateThesis(UUID id, ThesisDto thesisDto);
    void deleteThesis(UUID id);
    
    // File upload functionality
    ThesisDto uploadThesisPdf(UUID thesisId, MultipartFile pdfFile);
    
    // Grade and status management
    ThesisDto updateGrade(UUID thesisId, BigDecimal grade);
    ThesisDto updateStatus(UUID thesisId, ThesisStatus status);
    
    // Search and query methods based on requirements
    List<ThesisDto> findThesesByTitleContaining(String titleText);
    List<ThesisDto> findThesesByTeacherId(UUID teacherId);
    List<ThesisDto> findThesesDefendedInPeriod(OffsetDateTime startDate, OffsetDateTime endDate);
    List<ThesisDto> findThesesByGradeRange(BigDecimal minGrade, BigDecimal maxGrade);
    
    // Statistics methods based on requirements
    long countSuccessfulDefensesByTeacher(UUID teacherId);
    long countThesesByStatus(ThesisStatus status);
    
    // Additional utility methods
    boolean hasStudentSubmittedThesis(UUID studentId);
    List<ThesisDto> findThesesAwaitingReview();
    List<ThesisDto> findThesesAwaitingDefense();
    
    // Student workflow methods
    ThesisDto proceedToDefense(UUID thesisId);
    boolean canStudentProceedToDefense(UUID thesisId);
}
