package com.uni.ethesis.service;

import java.util.List;
import java.util.UUID;

import com.uni.ethesis.data.dto.DepartmentDto;

public interface DepartmentService {
    DepartmentDto createDepartment(DepartmentDto departmentDto);
    DepartmentDto getDepartmentById(UUID id);
    DepartmentDto getDepartmentByName(String name);
    List<DepartmentDto> getAllDepartments();
    DepartmentDto updateDepartment(UUID id, DepartmentDto departmentDto);
    void deleteDepartment(UUID id);
    List<DepartmentDto> getDepartmentsByUserId(UUID userId);
    List<DepartmentDto> getDepartmentsByHeadId(UUID headId);
    DepartmentDto getDepartmentByCurrentHeadId(UUID headId);
    
    // Department appointment management methods
    /**
     * Appoint a user as department head
     * @param departmentId The department ID
     * @param userId The user ID to appoint as head
     * @param startDate The appointment start date
     * @param endDate The appointment end date (null for indefinite)
     * @return The updated department DTO
     */
    DepartmentDto appointDepartmentHead(UUID departmentId, UUID userId, java.time.OffsetDateTime startDate, java.time.OffsetDateTime endDate);
    
    /**
     * End the current department head appointment
     * @param departmentId The department ID
     * @param endDate The end date for the appointment
     * @return The updated department DTO
     */
    DepartmentDto endCurrentHeadAppointment(UUID departmentId, java.time.OffsetDateTime endDate);
    
    /**
     * Get current department head for a department
     * @param departmentId The department ID
     * @return The current head's user information, or null if no current head
     */
    com.uni.ethesis.data.dto.UserDto getCurrentDepartmentHead(UUID departmentId);
    
    /**
     * Get appointment history for a department
     * @param departmentId The department ID
     * @return List of all head appointments for the department
     */
    List<com.uni.ethesis.data.dto.DepartmentAppointmentDto> getDepartmentAppointmentHistory(UUID departmentId);
    
    // Regular department membership methods (UserInDepartment)
    /**
     * Add a user as a regular member to a department
     * @param departmentId The department ID
     * @param userId The user ID to add
     * @return The updated department DTO
     */
    DepartmentDto addUserToDepartment(UUID departmentId, UUID userId);
    
    /**
     * Remove a user from department membership
     * @param departmentId The department ID
     * @param userId The user ID to remove
     * @return The updated department DTO
     */
    DepartmentDto removeUserFromDepartment(UUID departmentId, UUID userId);
    
    /**
     * Check if a user is a member of a department
     * @param departmentId The department ID
     * @param userId The user ID
     * @return true if user is a member, false otherwise
     */
    boolean isUserMemberOfDepartment(UUID departmentId, UUID userId);
    
    /**
     * Get all users who are members of a department
     * @param departmentId The department ID
     * @return List of users in the department
     */
    List<com.uni.ethesis.data.dto.UserDto> getDepartmentMembers(UUID departmentId);
}
