package com.uni.ethesis.data.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uni.ethesis.data.entities.Student;
import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.enums.StudentType;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {
    Student findByUniversityIdIgnoreCase(String universityId);
    Student findByUserEmailIgnoreCase(String email);
    List<Student> findByUserFirstNameIgnoreCase(String firstName);
    List<Student> findByUserLastNameIgnoreCase(String lastName);
    List<Student> findByUserFirstNameAndUserLastNameAllIgnoreCase(String firstName, String lastName);
    List<Student> findStudentByStudentType(StudentType studentType);
    @Query("SELECT s FROM Student s JOIN s.user u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :text, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :text, '%'))")
    List<Student> findByUserFirstNameOrLastNameContainingIgnoreCase(@Param("text") String text);
    Optional<Student> findByUser(User user);
}