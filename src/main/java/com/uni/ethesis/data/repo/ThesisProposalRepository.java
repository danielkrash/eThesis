package com.uni.ethesis.data.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.uni.ethesis.data.entities.ThesisProposal;
import com.uni.ethesis.enums.ThesisProposalStatus;

public interface ThesisProposalRepository extends JpaRepository<ThesisProposal, UUID>, JpaSpecificationExecutor<ThesisProposal> {
    List<ThesisProposal> findByStudentId(UUID studentId);
    List<ThesisProposal> findByTeacherId(UUID teacherId);
    List<ThesisProposal> findByStatus(ThesisProposalStatus status);
}