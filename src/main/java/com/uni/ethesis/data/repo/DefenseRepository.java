package com.uni.ethesis.data.repo;

import com.uni.ethesis.data.entities.Defense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface DefenseRepository extends JpaRepository<Defense, UUID>, JpaSpecificationExecutor<Defense> {
}