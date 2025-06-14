package com.uni.ethesis.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.data.entities.ThesisProposal;
import com.uni.ethesis.data.repo.StudentRepository;
import com.uni.ethesis.data.repo.TeacherRepository;
import com.uni.ethesis.data.repo.ThesisProposalRepository;
import com.uni.ethesis.enums.ThesisProposalStatus;
import com.uni.ethesis.exceptions.ThesisProposalNotFoundException;
import com.uni.ethesis.service.ThesisApplicationService;
import com.uni.ethesis.utils.mappers.ThesisApplicationMapper;

@Service
public class ThesisApplicationServiceImpl implements ThesisApplicationService {

    private final ThesisProposalRepository thesisProposalRepository;
    private final ThesisApplicationMapper thesisApplicationMapper;
    private final StudentRepository studentRepository; // For @Context in mapper
    private final TeacherRepository teacherRepository; // For @Context in mapper

    @Autowired
    public ThesisApplicationServiceImpl(ThesisProposalRepository thesisProposalRepository,
                                        ThesisApplicationMapper thesisApplicationMapper,
                                        StudentRepository studentRepository,
                                        TeacherRepository teacherRepository) {
        this.thesisProposalRepository = thesisProposalRepository;
        this.thesisApplicationMapper = thesisApplicationMapper;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    @Override
    @Transactional
    public ThesisProposalDto createThesisProposal(ThesisProposalDto thesisProposalDto) {
        ThesisProposal thesisProposal = thesisApplicationMapper.thesisProposalDtoToThesisProposal(
                thesisProposalDto, studentRepository, teacherRepository);
        ThesisProposal savedProposal = thesisProposalRepository.save(thesisProposal);
        return thesisApplicationMapper.thesisProposalToThesisProposalDto(savedProposal);
    }

    @Override
    public ThesisProposalDto getThesisProposalById(UUID id) {
        ThesisProposal proposal = thesisProposalRepository.findById(id)
                .orElseThrow(() -> new ThesisProposalNotFoundException("Thesis proposal not found with id: " + id));
        return thesisApplicationMapper.thesisProposalToThesisProposalDto(proposal);
    }

    @Override
    public List<ThesisProposalDto> getAllThesisProposals() {
        return thesisProposalRepository.findAll().stream()
                .map(thesisApplicationMapper::thesisProposalToThesisProposalDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ThesisProposalDto> getThesisProposalsByStudentId(UUID studentId) {
        return thesisProposalRepository.findByStudentId(studentId).stream()
                .map(thesisApplicationMapper::thesisProposalToThesisProposalDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ThesisProposalDto> getThesisProposalsByTeacherId(UUID teacherId) {
        return thesisProposalRepository.findByTeacherId(teacherId).stream()
                .map(thesisApplicationMapper::thesisProposalToThesisProposalDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ThesisProposalDto> getThesisProposalsByStatus(ThesisProposalStatus status) {
        return thesisProposalRepository.findByStatus(status).stream()
                .map(thesisApplicationMapper::thesisProposalToThesisProposalDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ThesisProposalDto updateThesisProposal(UUID id, ThesisProposalDto thesisProposalDto) {
        return thesisProposalRepository.findById(id)
                .map(existingProposal -> {
                    thesisApplicationMapper.updateThesisProposalFromDto(thesisProposalDto, existingProposal, studentRepository, teacherRepository);
                    ThesisProposal updatedProposal = thesisProposalRepository.save(existingProposal);
                    return thesisApplicationMapper.thesisProposalToThesisProposalDto(updatedProposal);
                })
                .orElseThrow(() -> new ThesisProposalNotFoundException("Thesis proposal not found with id: " + id));
    }

    @Override
    @Transactional
    public ThesisProposalDto updateThesisProposalStatus(UUID id, ThesisProposalStatus status) {
        ThesisProposal proposal = thesisProposalRepository.findById(id)
                .orElseThrow(() -> new ThesisProposalNotFoundException("Thesis proposal not found with id: " + id));
        proposal.setStatus(status);
        ThesisProposal updatedProposal = thesisProposalRepository.save(proposal);
        return thesisApplicationMapper.thesisProposalToThesisProposalDto(updatedProposal);
    }

    @Override
    @Transactional
    public void deleteThesisProposal(UUID id) {
        ThesisProposal proposal = thesisProposalRepository.findById(id)
                .orElseThrow(() -> new ThesisProposalNotFoundException("Thesis proposal not found with id: " + id));
        thesisProposalRepository.delete(proposal);
    }
}
