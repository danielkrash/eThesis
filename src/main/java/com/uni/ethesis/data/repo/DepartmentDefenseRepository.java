package com.uni.ethesis.data.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uni.ethesis.data.entities.DepartmentDefense;

public interface DepartmentDefenseRepository extends JpaRepository<DepartmentDefense, UUID>, JpaSpecificationExecutor<DepartmentDefense> {
    
    // Find department defenses by defense ID
    List<DepartmentDefense> findByDefenseId(UUID defenseId);
    
    // Find department defenses by department ID
    List<DepartmentDefense> findByDepartmentId(UUID departmentId);
    
    // Check if a department is associated with a defense
    boolean existsByDepartmentIdAndDefenseId(UUID departmentId, UUID defenseId);
    
    // Get all departments for a specific defense
    @Query("SELECT dd.department FROM DepartmentDefense dd WHERE dd.defense.id = :defenseId")
    List<com.uni.ethesis.data.entities.Department> findDepartmentsByDefenseId(@Param("defenseId") UUID defenseId);
    
    // Get all defenses for a specific department
    @Query("SELECT dd.defense FROM DepartmentDefense dd WHERE dd.department.id = :departmentId")
    List<com.uni.ethesis.data.entities.Defense> findDefensesByDepartmentId(@Param("departmentId") UUID departmentId);
}
