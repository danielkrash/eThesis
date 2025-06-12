package com.uni.ethesis.data.repo;

import com.uni.ethesis.data.entities.Thesis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ThesisRepository extends JpaRepository<Thesis, UUID> , JpaSpecificationExecutor<Thesis> {
}