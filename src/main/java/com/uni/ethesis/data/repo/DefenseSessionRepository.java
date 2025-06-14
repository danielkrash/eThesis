package com.uni.ethesis.data.repo;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uni.ethesis.data.entities.DefenseSession;

public interface DefenseSessionRepository extends JpaRepository<DefenseSession, UUID>, JpaSpecificationExecutor<DefenseSession> {
    
    // Find defense session by thesis ID
    Optional<DefenseSession> findByThesisId(UUID thesisId);
    
    // Find defense sessions by defense ID
    List<DefenseSession> findByDefenseId(UUID defenseId);
    
    // Find defense sessions in date range
    @Query("SELECT ds FROM DefenseSession ds WHERE ds.dateAndTime BETWEEN :startDate AND :endDate")
    List<DefenseSession> findByDateAndTimeBetween(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    // Count students defended in period (based on requirements)
    @Query("SELECT COUNT(ds) FROM DefenseSession ds WHERE ds.dateAndTime BETWEEN :startDate AND :endDate")
    long countStudentsDefendedInPeriod(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    // Get average attendance in period (assuming number of sessions = attendance)
    @Query("SELECT AVG(CAST((SELECT COUNT(dsp) FROM DefenseSessionProfessor dsp WHERE dsp.defenseSession.id = ds.id) AS double)) " +
           "FROM DefenseSession ds WHERE ds.dateAndTime BETWEEN :startDate AND :endDate")
    Double getAverageAttendanceInPeriod(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    // Find sessions by teacher/professor
    @Query("SELECT ds FROM DefenseSession ds JOIN ds.professors dsp WHERE dsp.professor.id = :teacherId")
    List<DefenseSession> findByTeacherId(@Param("teacherId") UUID teacherId);
}