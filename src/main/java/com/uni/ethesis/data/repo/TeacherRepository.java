package com.uni.ethesis.data.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.uni.ethesis.data.entities.Teacher;
import com.uni.ethesis.data.entities.User;

public interface TeacherRepository extends JpaRepository<Teacher, UUID>, JpaSpecificationExecutor<Teacher> {
    Teacher findByUserEmailIgnoreCase(String email);
    Teacher findByUserId(UUID userId);
    Teacher findByUser(User user);
}