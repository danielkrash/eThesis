package com.uni.ethesis.data.repo;

import com.uni.ethesis.data.entities.ThesisProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ThesisProposalRepository extends JpaRepository<ThesisProposal, UUID>, JpaSpecificationExecutor<ThesisProposal> {
}