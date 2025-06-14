package com.uni.ethesis.service;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import com.uni.ethesis.data.dto.DefenseDto;

public interface DefenseService {
    
    // Defense CRUD operations
    DefenseDto createDefense(DefenseDto defenseDto);
    DefenseDto getDefenseById(UUID id);
    List<DefenseDto> getAllDefenses();
    List<DefenseDto> getDefensesByDate(Date date);
    List<DefenseDto> getDefensesByDateRange(Date startDate, Date endDate);
    DefenseDto updateDefense(UUID id, DefenseDto defenseDto);
    void deleteDefense(UUID id);
    
    // Statistics methods based on requirements
    long countSuccessfulDefensesByTeacher(UUID teacherId);
    
    // Query methods
    List<DefenseDto> getDefensesByLocation(String location);
    long countDefensesInPeriod(Date startDate, Date endDate);
}
