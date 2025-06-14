package com.uni.ethesis.data.repo;

import com.uni.ethesis.data.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime; // Added import
import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> , JpaSpecificationExecutor<User> {
    List<User> findUsersByFirstNameAndLastName(String firstName, String lastName);
    List<User> findUsersByFirstName(String firstName);
    List<User> findUsersByLastName(String lastName);
    List<User> findAll();
    User findUserByEmail(String email);
    User findUserById(UUID id);
    List<User> findUsersByEmailContaining(String partialEmail);
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :text, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :text, '%'))")
    List<User> searchByName(@Param("text") String text);
    @Query("SELECT u FROM User u JOIN u.departments uid JOIN uid.department d WHERE LOWER(d.name) = LOWER(:departmentName)")
    List<User> findUsersByDepartmentName(@Param("departmentName") String departmentName);
    @Query("SELECT u FROM User u JOIN u.departments uid WHERE uid.department.id = :departmentId")
    List<User> findUsersByDepartmentId(@Param("departmentId") UUID departmentId);

    // Query 1: Users who were ever a department head (had any appointment)
    @Query("SELECT DISTINCT u FROM User u JOIN u.appointments app")
    List<User> findUsersWithAnyDepartmentAppointment();

    // Query 2: Users who were department head from a specific start date to a specific end date (appointment overlaps the range)
    @Query("SELECT DISTINCT u FROM User u JOIN u.appointments app WHERE app.startDate <= :endDate AND (app.endDate IS NULL OR app.endDate >= :startDate)")
    List<User> findUsersWithDepartmentAppointmentInDateRange(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    // Query 3: Users who were department head of a specific department (by department name)
    @Query("SELECT DISTINCT u FROM User u JOIN u.appointments app WHERE app.department.name = :departmentName")
    List<User> findUsersWithDepartmentAppointmentForDepartmentName(@Param("departmentName") String departmentName);

    // Query 4: Users who were department head of a specific department (by department id)
    @Query("SELECT DISTINCT u FROM User u JOIN u.appointments app WHERE app.department.id = :departmentId")
    List<User> findUsersWithDepartmentAppointmentForDepartmentId(@Param("departmentId") UUID departmentId);

    // Query 5: Users who currently are department head of a specific department (by department name)
    @Query("SELECT DISTINCT u FROM User u JOIN u.appointments app WHERE app.department.name = :departmentName AND (app.endDate IS NULL OR app.endDate > :currentDate)")
    List<User> findCurrentUsersWithDepartmentAppointmentForDepartmentName(@Param("departmentName") String departmentName , @Param("currentDate") OffsetDateTime currentDate);

    // Query 6: Users who currently are department head of a specific department (by department id)
    @Query("SELECT DISTINCT u FROM User u JOIN u.appointments app WHERE app.department.id = :departmentId AND (app.endDate IS NULL OR app.endDate > :currentDate)")
    List<User> findCurrentUsersWithDepartmentAppointmentForDepartmentId(@Param("departmentId") UUID departmentId, @Param("currentDate") OffsetDateTime currentDate);
}