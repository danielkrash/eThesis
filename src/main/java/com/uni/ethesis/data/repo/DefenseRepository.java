package com.uni.ethesis.data.repo;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uni.ethesis.data.entities.Defense;

public interface DefenseRepository extends JpaRepository<Defense, UUID>, JpaSpecificationExecutor<Defense> {
    
    // Find defenses by date
    List<Defense> findByDate(Date date);
    
    // Find defenses in date range
    List<Defense> findByDateBetween(Date startDate, Date endDate);
    
    // Find defenses by location
    List<Defense> findByLocationIgnoreCase(String location);
    
    // Count defenses in period
    @Query("SELECT COUNT(d) FROM Defense d WHERE d.date BETWEEN :startDate AND :endDate")
    long countDefensesInPeriod(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}