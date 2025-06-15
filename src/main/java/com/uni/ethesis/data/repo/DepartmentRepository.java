package com.uni.ethesis.data.repo;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uni.ethesis.data.entities.Department;

public interface DepartmentRepository extends JpaRepository<Department, UUID>, JpaSpecificationExecutor<Department> {
    Department findByNameIgnoreCase(String name);

    // Assuming UserInDepartment entity links users to departments
    // and Department entity has a 'users' collection of UserInDepartment
    @Query("SELECT d FROM Department d JOIN d.users uid WHERE uid.user.id = :userId")
    List<Department> findDepartmentsByUserId(@Param("userId") UUID userId);

    @Query("SELECT da.department FROM DepartmentAppointment da WHERE da.user.id = :headId")
    List<Department> findDepartmentsByHeadId(@Param("headId") UUID headId);

    @Query("SELECT da.department FROM DepartmentAppointment da WHERE da.user.id = :headId AND da.startDate <= :currentDate AND (da.endDate IS NULL OR da.endDate >= :currentDate)")
    Department findDepartmentByCurrentHeadId(@Param("headId") UUID headId, @Param("currentDate") OffsetDateTime currentDate);
}