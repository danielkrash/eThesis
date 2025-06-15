package com.uni.ethesis.data.repo;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uni.ethesis.data.entities.DepartmentAppointment;

public interface DepartmentAppointmentRepository extends JpaRepository<DepartmentAppointment, UUID>, JpaSpecificationExecutor<DepartmentAppointment> {
    
    /**
     * Find all appointments for a specific department, ordered by start date
     */
    @Query("SELECT da FROM DepartmentAppointment da WHERE da.department.id = :departmentId ORDER BY da.startDate DESC")
    List<DepartmentAppointment> findByDepartmentIdOrderByStartDateDesc(@Param("departmentId") UUID departmentId);
    
    /**
     * Find all appointments for a specific user
     */
    @Query("SELECT da FROM DepartmentAppointment da WHERE da.user.id = :userId ORDER BY da.startDate DESC")
    List<DepartmentAppointment> findByUserIdOrderByStartDateDesc(@Param("userId") UUID userId);
    
    /**
     * Find current appointment for a department (startDate <= now AND (endDate IS NULL OR endDate >= now))
     */
    @Query("SELECT da FROM DepartmentAppointment da WHERE da.department.id = :departmentId " +
           "AND da.startDate <= :currentDate AND (da.endDate IS NULL OR da.endDate >= :currentDate)")
    Optional<DepartmentAppointment> findCurrentAppointmentByDepartmentId(@Param("departmentId") UUID departmentId, 
                                                                          @Param("currentDate") OffsetDateTime currentDate);
    
    /**
     * Find current appointments for a user (across all departments)
     */
    @Query("SELECT da FROM DepartmentAppointment da WHERE da.user.id = :userId " +
           "AND da.startDate <= :currentDate AND (da.endDate IS NULL OR da.endDate >= :currentDate)")
    List<DepartmentAppointment> findCurrentAppointmentsByUserId(@Param("userId") UUID userId, 
                                                                 @Param("currentDate") OffsetDateTime currentDate);
    
    /**
     * Check if a user has a current appointment in any department
     */
    @Query("SELECT COUNT(da) > 0 FROM DepartmentAppointment da WHERE da.user.id = :userId " +
           "AND da.startDate <= :currentDate AND (da.endDate IS NULL OR da.endDate >= :currentDate)")
    boolean hasCurrentAppointment(@Param("userId") UUID userId, @Param("currentDate") OffsetDateTime currentDate);
    
    /**
     * Check if a department has a current head
     */
    @Query("SELECT COUNT(da) > 0 FROM DepartmentAppointment da WHERE da.department.id = :departmentId " +
           "AND da.startDate <= :currentDate AND (da.endDate IS NULL OR da.endDate >= :currentDate)")
    boolean hasCurrentHead(@Param("departmentId") UUID departmentId, @Param("currentDate") OffsetDateTime currentDate);
    
    /**
     * Find appointments that overlap with a given time period
     */
    @Query("SELECT da FROM DepartmentAppointment da WHERE da.department.id = :departmentId " +
           "AND da.startDate < :endDate AND (da.endDate IS NULL OR da.endDate > :startDate)")
    List<DepartmentAppointment> findOverlappingAppointments(@Param("departmentId") UUID departmentId,
                                                             @Param("startDate") OffsetDateTime startDate,
                                                             @Param("endDate") OffsetDateTime endDate);
}
