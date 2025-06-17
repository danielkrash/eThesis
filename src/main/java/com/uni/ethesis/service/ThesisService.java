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
    boolean thesisExists(UUID id);
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
    
    // New methods for READY_FOR_DEFENSE status
    /**
     * Mark thesis as ready for defense after positive review
     * @param thesisId The thesis ID
     * @return Updated thesis DTO
     */
    ThesisDto markThesisReadyForDefense(UUID thesisId);
    
    /**
     * Check if thesis can be marked as ready for defense
     * @param thesisId The thesis ID
     * @return true if can be marked ready
     */
    boolean canMarkThesisReadyForDefense(UUID thesisId);
    
    /**
     * Get all theses that are ready for defense (awaiting defense scheduling)
     * @return List of theses ready for defense
     */
    List<ThesisDto> findThesesReadyForDefense();
    
    /**
     * Get all theses by status
     * @param status The thesis status to filter by
     * @return List of theses with the given status
     */
    List<ThesisDto> getThesesByStatus(ThesisStatus status);
    
    /**
     * Get theses by status and department
     * @param status The thesis status to filter by
     * @param departmentId The department ID to filter by
     * @return List of theses with the given status and department
     */
    List<ThesisDto> getThesesByStatusAndDepartment(ThesisStatus status, UUID departmentId);
}
