package com.uni.ethesis.data.repo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uni.ethesis.data.entities.Thesis;
import com.uni.ethesis.enums.ThesisStatus;

public interface ThesisRepository extends JpaRepository<Thesis, UUID>, JpaSpecificationExecutor<Thesis> {
    
    // Find thesis by proposal ID
    Optional<Thesis> findByProposalId(UUID proposalId);
    
    // Find thesis by student ID
    @Query("SELECT t FROM Thesis t WHERE t.proposal.student.id = :studentId")
    Optional<Thesis> findByStudentId(@Param("studentId") UUID studentId);
    
    // Find theses by teacher ID (supervisor)
    @Query("SELECT t FROM Thesis t WHERE t.proposal.teacher.id = :teacherId")
    List<Thesis> findByTeacherId(@Param("teacherId") UUID teacherId);
    
    // Find theses by title containing text (case insensitive)
    @Query("SELECT t FROM Thesis t WHERE LOWER(t.proposal.title) LIKE LOWER(CONCAT('%', :titleText, '%'))")
    List<Thesis> findByTitleContaining(@Param("titleText") String titleText);
    
    // Find theses defended in a time period
    @Query("SELECT t FROM Thesis t WHERE t.status = :status AND t.lastModifiedAt BETWEEN :startDate AND :endDate")
    List<Thesis> findByStatusAndDateRange(@Param("status") ThesisStatus status, 
                                          @Param("startDate") OffsetDateTime startDate, 
                                          @Param("endDate") OffsetDateTime endDate);
    
    // Find theses with grade in range
    @Query("SELECT t FROM Thesis t WHERE t.finalGrade BETWEEN :minGrade AND :maxGrade")
    List<Thesis> findByGradeRange(@Param("minGrade") BigDecimal minGrade, 
                                  @Param("maxGrade") BigDecimal maxGrade);
    
    // Count theses by status
    long countByStatus(ThesisStatus status);
    
    // Find theses by status
    List<Thesis> findByStatus(ThesisStatus status);
    
    // Find theses waiting for defense by department
    @Query("SELECT t FROM Thesis t WHERE t.status = :status AND t.proposal.department.id = :departmentId")
    List<Thesis> findByStatusAndDepartmentId(@Param("status") ThesisStatus status, @Param("departmentId") UUID departmentId);
    
    // Count successfully defended theses by teacher
    @Query("SELECT COUNT(t) FROM Thesis t WHERE t.proposal.teacher.id = :teacherId AND t.status = 'DEFENDED' AND t.finalGrade >= :minPassingGrade")
    long countSuccessfulDefensesByTeacher(@Param("teacherId") UUID teacherId, 
                                          @Param("minPassingGrade") BigDecimal minPassingGrade);
}