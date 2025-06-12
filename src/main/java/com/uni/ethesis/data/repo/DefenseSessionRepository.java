package com.uni.ethesis.data.repo;

import com.uni.ethesis.data.entities.DefenseSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface DefenseSessionRepository extends JpaRepository<DefenseSession, UUID>, JpaSpecificationExecutor<DefenseSession> {
}