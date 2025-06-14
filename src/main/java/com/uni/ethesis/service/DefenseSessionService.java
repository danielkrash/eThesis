package com.uni.ethesis.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.uni.ethesis.data.dto.DefenseSessionDto;

public interface DefenseSessionService {
    
    // Defense Session CRUD operations
    DefenseSessionDto createDefenseSession(DefenseSessionDto defenseSessionDto);
    DefenseSessionDto getDefenseSessionById(UUID id);
    DefenseSessionDto getDefenseSessionByThesisId(UUID thesisId);
    List<DefenseSessionDto> getDefenseSessionsByDefenseId(UUID defenseId);
    List<DefenseSessionDto> getAllDefenseSessions();
    DefenseSessionDto updateDefenseSession(UUID id, DefenseSessionDto defenseSessionDto);
    void deleteDefenseSession(UUID id);
    
    // Defense workflow methods
    DefenseSessionDto scheduleDefenseForThesis(UUID thesisId, UUID defenseId, OffsetDateTime dateTime, String notes);
    DefenseSessionDto recordDefenseGrade(UUID defenseSessionId, BigDecimal finalGrade);
    boolean isThesisScheduledForDefense(UUID thesisId);
    
    // Query methods
    List<DefenseSessionDto> getDefenseSessionsByTeacher(UUID teacherId);
    List<DefenseSessionDto> getDefenseSessionsInPeriod(OffsetDateTime startDate, OffsetDateTime endDate);
    
    // Statistics methods
    long countStudentsDefendedInPeriod(OffsetDateTime startDate, OffsetDateTime endDate);
    double getAverageAttendanceInPeriod(OffsetDateTime startDate, OffsetDateTime endDate);
}
