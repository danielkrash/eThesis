package com.uni.ethesis.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.uni.ethesis.data.repo.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.data.entities.ThesisProposal;
import com.uni.ethesis.data.repo.StudentRepository;
import com.uni.ethesis.data.repo.TeacherRepository;
import com.uni.ethesis.data.repo.ThesisProposalRepository;
import com.uni.ethesis.enums.ThesisProposalStatus;
import com.uni.ethesis.exceptions.InvalidStatusTransitionException;
import com.uni.ethesis.exceptions.ThesisProposalNotFoundException;
import com.uni.ethesis.service.ThesisProposalService;
import com.uni.ethesis.utils.mappers.ThesisProposalMapper;

@Service
public class ThesisProposalServiceImpl implements ThesisProposalService {

    private final ThesisProposalRepository thesisProposalRepository;
    private final ThesisProposalMapper thesisProposalMapper;
    private final StudentRepository studentRepository; // For @Context in mapper
    private final TeacherRepository teacherRepository; // For @Context in mapper
    private final DepartmentRepository departmentRepository;

    @Autowired
    public ThesisProposalServiceImpl(ThesisProposalRepository thesisProposalRepository,
                                     ThesisProposalMapper thesisProposalMapper,
                                     StudentRepository studentRepository,
                                     TeacherRepository teacherRepository,
                                     DepartmentRepository departmentRepository) {
        this.thesisProposalRepository = thesisProposalRepository;
        this.thesisProposalMapper = thesisProposalMapper;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public ThesisProposalDto createThesisProposal(ThesisProposalDto thesisProposalDto) {
        ThesisProposal thesisProposal = thesisProposalMapper.thesisProposalDtoToThesisProposal(
                thesisProposalDto, studentRepository, teacherRepository , departmentRepository);
        ThesisProposal savedProposal = thesisProposalRepository.save(thesisProposal);
        return thesisProposalMapper.thesisProposalToThesisProposalDto(savedProposal);
    }

    @Override
    public ThesisProposalDto getThesisProposalById(UUID id) {
        ThesisProposal proposal = thesisProposalRepository.findById(id)
                .orElseThrow(() -> new ThesisProposalNotFoundException("Thesis proposal not found with id: " + id));
        return thesisProposalMapper.thesisProposalToThesisProposalDto(proposal);
    }

    @Override
    public List<ThesisProposalDto> getAllThesisProposals() {
        return thesisProposalRepository.findAll().stream()
                .map(thesisProposalMapper::thesisProposalToThesisProposalDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ThesisProposalDto> getThesisProposalsByStudentId(UUID studentId) {
        return thesisProposalRepository.findByStudentId(studentId).stream()
                .map(thesisProposalMapper::thesisProposalToThesisProposalDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ThesisProposalDto> getThesisProposalsByTeacherId(UUID teacherId) {
        return thesisProposalRepository.findByTeacherId(teacherId).stream()
                .map(thesisProposalMapper::thesisProposalToThesisProposalDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ThesisProposalDto> getThesisProposalsByStatus(ThesisProposalStatus status) {
        return thesisProposalRepository.findByStatus(status).stream()
                .map(thesisProposalMapper::thesisProposalToThesisProposalDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ThesisProposalDto updateThesisProposal(UUID id, ThesisProposalDto thesisProposalDto) {
        return thesisProposalRepository.findById(id)
                .map(existingProposal -> {
                    thesisProposalMapper.updateThesisProposalFromDto(thesisProposalDto, existingProposal, studentRepository, teacherRepository , departmentRepository);
                    ThesisProposal updatedProposal = thesisProposalRepository.save(existingProposal);
                    return thesisProposalMapper.thesisProposalToThesisProposalDto(updatedProposal);
                })
                .orElseThrow(() -> new ThesisProposalNotFoundException("Thesis proposal not found with id: " + id));
    }

    @Override
    @Transactional
    public ThesisProposalDto updateThesisProposalStatus(UUID id, ThesisProposalStatus status) {
        ThesisProposal proposal = thesisProposalRepository.findById(id)
                .orElseThrow(() -> new ThesisProposalNotFoundException("Thesis proposal not found with id: " + id));
        
        ThesisProposalStatus currentStatus = proposal.getStatus();
        
        // If current status is null, allow transition to any status
        if (currentStatus == null) {
            proposal.setStatus(status);
            ThesisProposal updatedProposal = thesisProposalRepository.save(proposal);
            return thesisProposalMapper.thesisProposalToThesisProposalDto(updatedProposal);
        }
        
        // If trying to set PENDING when already PENDING, that's invalid
        if (currentStatus == ThesisProposalStatus.PENDING && status == ThesisProposalStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                String.format("Invalid target status: %s", status.name()));
        }
        
        // Can only change from PENDING to REJECTED or APPROVED
        if (currentStatus == ThesisProposalStatus.PENDING && 
            (status == ThesisProposalStatus.REJECTED || status == ThesisProposalStatus.APPROVED)) {
            proposal.setStatus(status);
            ThesisProposal updatedProposal = thesisProposalRepository.save(proposal);
            return thesisProposalMapper.thesisProposalToThesisProposalDto(updatedProposal);
        }
        
        // Cannot change from REJECTED or APPROVED to anything else
        if (currentStatus == ThesisProposalStatus.REJECTED || currentStatus == ThesisProposalStatus.APPROVED) {
            throw new InvalidStatusTransitionException(
                String.format("Cannot change thesis proposal status from %s to %s",
                    currentStatus.name(), status.name()));
        }
        
        // Fallback for any other invalid transitions
        throw new InvalidStatusTransitionException(
            String.format("Invalid target status: %s", status.name()));
    }

    @Override
    @Transactional
    public void deleteThesisProposal(UUID id) {
        ThesisProposal proposal = thesisProposalRepository.findById(id)
                .orElseThrow(() -> new ThesisProposalNotFoundException("Thesis proposal not found with id: " + id));
        thesisProposalRepository.delete(proposal);
    }
}
