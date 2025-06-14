package com.uni.ethesis.data.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uni.ethesis.data.entities.UserInDepartment;

public interface UserInDepartmentRepository extends JpaRepository<UserInDepartment, UUID>, JpaSpecificationExecutor<UserInDepartment> {
    
    /**
     * Find all users in a specific department
     */
    @Query("SELECT uid FROM UserInDepartment uid WHERE uid.department.id = :departmentId")
    List<UserInDepartment> findByDepartmentId(@Param("departmentId") UUID departmentId);
    
    /**
     * Find all departments a user belongs to
     */
    @Query("SELECT uid FROM UserInDepartment uid WHERE uid.user.id = :userId")
    List<UserInDepartment> findByUserId(@Param("userId") UUID userId);
    
    /**
     * Find specific user-department relationship
     */
    @Query("SELECT uid FROM UserInDepartment uid WHERE uid.department.id = :departmentId AND uid.user.id = :userId")
    Optional<UserInDepartment> findByDepartmentIdAndUserId(@Param("departmentId") UUID departmentId, @Param("userId") UUID userId);
    
    /**
     * Check if a user exists in a department
     */
    @Query("SELECT COUNT(uid) > 0 FROM UserInDepartment uid WHERE uid.department.id = :departmentId AND uid.user.id = :userId")
    boolean existsByDepartmentIdAndUserId(@Param("departmentId") UUID departmentId, @Param("userId") UUID userId);
    
    /**
     * Count users in a department
     */
    @Query("SELECT COUNT(uid) FROM UserInDepartment uid WHERE uid.department.id = :departmentId")
    long countByDepartmentId(@Param("departmentId") UUID departmentId);
    
    /**
     * Delete user from department
     */
    @Query("DELETE FROM UserInDepartment uid WHERE uid.department.id = :departmentId AND uid.user.id = :userId")
    void deleteByDepartmentIdAndUserId(@Param("departmentId") UUID departmentId, @Param("userId") UUID userId);
}
