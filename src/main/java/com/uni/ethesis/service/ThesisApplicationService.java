package com.uni.ethesis.service;

import java.util.List;
import java.util.UUID;

import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.enums.ThesisProposalStatus;

public interface ThesisApplicationService {
    ThesisProposalDto createThesisProposal(ThesisProposalDto thesisProposalDto);
    ThesisProposalDto getThesisProposalById(UUID id);
    List<ThesisProposalDto> getAllThesisProposals();
    List<ThesisProposalDto> getThesisProposalsByStudentId(UUID studentId);
    List<ThesisProposalDto> getThesisProposalsByTeacherId(UUID teacherId);
    List<ThesisProposalDto> getThesisProposalsByStatus(ThesisProposalStatus status);
    ThesisProposalDto updateThesisProposal(UUID id, ThesisProposalDto thesisProposalDto);
    ThesisProposalDto updateThesisProposalStatus(UUID id, ThesisProposalStatus status);
    void deleteThesisProposal(UUID id);
}
