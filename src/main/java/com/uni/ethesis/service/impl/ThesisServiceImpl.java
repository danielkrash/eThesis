package com.uni.ethesis.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.uni.ethesis.data.dto.ThesisDto;
import com.uni.ethesis.data.entities.Thesis;
import com.uni.ethesis.data.entities.ThesisProposal;
import com.uni.ethesis.data.repo.ThesisProposalRepository;
import com.uni.ethesis.data.repo.ThesisRepository;
import com.uni.ethesis.enums.ThesisStatus;
import com.uni.ethesis.exceptions.FileUploadException;
import com.uni.ethesis.exceptions.ThesisNotFoundException;
import com.uni.ethesis.exceptions.ThesisProposalNotFoundException;
import com.uni.ethesis.service.ThesisService;
import com.uni.ethesis.utils.mappers.ThesisMapper;

@Service
public class ThesisServiceImpl implements ThesisService {

    private final ThesisRepository thesisRepository;
    private final ThesisProposalRepository thesisProposalRepository;
    private final ThesisMapper thesisMapper;

    @Value("${app.thesis.upload-dir:uploads/theses}")
    private String uploadDir;

    private static final BigDecimal MIN_PASSING_GRADE = BigDecimal.valueOf(3.0);

    @Autowired
    public ThesisServiceImpl(ThesisRepository thesisRepository,
                             ThesisProposalRepository thesisProposalRepository,
                             ThesisMapper thesisMapper) {
        this.thesisRepository = thesisRepository;
        this.thesisProposalRepository = thesisProposalRepository;
        this.thesisMapper = thesisMapper;
    }

    @Override
    @Transactional
    public ThesisDto createThesis(UUID proposalId) {
        ThesisProposal proposal = thesisProposalRepository.findById(proposalId)
                .orElseThrow(() -> new ThesisProposalNotFoundException("Thesis proposal not found with id: " + proposalId));

        // Check if thesis already exists for this proposal
        if (thesisRepository.findByProposalId(proposalId).isPresent()) {
            throw new IllegalStateException("Thesis already exists for proposal with id: " + proposalId);
        }

        Thesis thesis = Thesis.builder()
                .proposal(proposal)
                .status(ThesisStatus.WAITING_FOR_REVIEW)
                .build();

        Thesis savedThesis = thesisRepository.save(thesis);
        return thesisMapper.thesisToThesisDto(savedThesis);
    }

    @Override
    public ThesisDto getThesisById(UUID id) {
        Thesis thesis = thesisRepository.findById(id)
                .orElseThrow(() -> new ThesisNotFoundException("Thesis not found with id: " + id));
        return thesisMapper.thesisToThesisDto(thesis);
    }

    @Override
    public ThesisDto getThesisByProposalId(UUID proposalId) {
        Thesis thesis = thesisRepository.findByProposalId(proposalId)
                .orElseThrow(() -> new ThesisNotFoundException("Thesis not found for proposal id: " + proposalId));
        return thesisMapper.thesisToThesisDto(thesis);
    }

    @Override
    public ThesisDto getThesisByStudentId(UUID studentId) {
        Thesis thesis = thesisRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ThesisNotFoundException("Thesis not found for student id: " + studentId));
        return thesisMapper.thesisToThesisDto(thesis);
    }

    @Override
    public List<ThesisDto> getAllTheses() {
        return thesisRepository.findAll().stream()
                .map(thesisMapper::thesisToThesisDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ThesisDto updateThesis(UUID id, ThesisDto thesisDto) {
        Thesis existingThesis = thesisRepository.findById(id)
                .orElseThrow(() -> new ThesisNotFoundException("Thesis not found with id: " + id));

        // Update only allowed fields
        if (thesisDto.getFinalGrade() != null) {
            existingThesis.setFinalGrade(thesisDto.getFinalGrade());
        }
        if (thesisDto.getStatus() != null) {
            existingThesis.setStatus(thesisDto.getStatus());
        }
        if (thesisDto.getPdfPath() != null) {
            existingThesis.setPdfPath(thesisDto.getPdfPath());
        }

        Thesis updatedThesis = thesisRepository.save(existingThesis);
        return thesisMapper.thesisToThesisDto(updatedThesis);
    }

    @Override
    @Transactional
    public void deleteThesis(UUID id) {
        if (!thesisRepository.existsById(id)) {
            throw new ThesisNotFoundException("Thesis not found with id: " + id);
        }
        thesisRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ThesisDto uploadThesisPdf(UUID thesisId, MultipartFile pdfFile) {
        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new ThesisNotFoundException("Thesis not found with id: " + thesisId));

        if (pdfFile.isEmpty()) {
            throw new FileUploadException("Please select a file to upload");
        }

        if (!isPdfFile(pdfFile)) {
            throw new FileUploadException("Only PDF files are allowed");
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = pdfFile.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                originalFilename = "thesis.pdf";
            }
            String fileExtension = originalFilename.contains(".") ? 
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".pdf";
            String filename = thesis.getId() + "_" + System.currentTimeMillis() + fileExtension;
            Path filePath = uploadPath.resolve(filename);

            // Copy file to target location
            Files.copy(pdfFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update thesis with file path
            thesis.setPdfPath(filePath.toString());
            thesis.setStatus(ThesisStatus.WAITING_FOR_REVIEW);

            Thesis updatedThesis = thesisRepository.save(thesis);
            return thesisMapper.thesisToThesisDto(updatedThesis);

        } catch (IOException e) {
            throw new FileUploadException("Could not upload file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ThesisDto updateGrade(UUID thesisId, BigDecimal grade) {
        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new ThesisNotFoundException("Thesis not found with id: " + thesisId));

        if (grade.compareTo(BigDecimal.ZERO) < 0 || grade.compareTo(BigDecimal.valueOf(6.0)) > 0) {
            throw new IllegalArgumentException("Grade must be between 0.0 and 6.0");
        }

        thesis.setFinalGrade(grade);
        
        // Update status based on grade
        if (grade.compareTo(MIN_PASSING_GRADE) >= 0) {
            thesis.setStatus(ThesisStatus.DEFENDED);
        } else {
            thesis.setStatus(ThesisStatus.FAILED);
        }

        Thesis updatedThesis = thesisRepository.save(thesis);
        return thesisMapper.thesisToThesisDto(updatedThesis);
    }

    @Override
    @Transactional
    public ThesisDto updateStatus(UUID thesisId, ThesisStatus status) {
        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new ThesisNotFoundException("Thesis not found with id: " + thesisId));

        thesis.setStatus(status);
        Thesis updatedThesis = thesisRepository.save(thesis);
        return thesisMapper.thesisToThesisDto(updatedThesis);
    }

    @Override
    public List<ThesisDto> findThesesByTitleContaining(String titleText) {
        return thesisRepository.findByTitleContaining(titleText).stream()
                .map(thesisMapper::thesisToThesisDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ThesisDto> findThesesByTeacherId(UUID teacherId) {
        return thesisRepository.findByTeacherId(teacherId).stream()
                .map(thesisMapper::thesisToThesisDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ThesisDto> findThesesDefendedInPeriod(OffsetDateTime startDate, OffsetDateTime endDate) {
        return thesisRepository.findByStatusAndDateRange(ThesisStatus.DEFENDED, startDate, endDate).stream()
                .map(thesisMapper::thesisToThesisDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ThesisDto> findThesesByGradeRange(BigDecimal minGrade, BigDecimal maxGrade) {
        return thesisRepository.findByGradeRange(minGrade, maxGrade).stream()
                .map(thesisMapper::thesisToThesisDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countSuccessfulDefensesByTeacher(UUID teacherId) {
        return thesisRepository.countSuccessfulDefensesByTeacher(teacherId, MIN_PASSING_GRADE);
    }

    @Override
    public long countThesesByStatus(ThesisStatus status) {
        return thesisRepository.countByStatus(status);
    }

    @Override
    public boolean hasStudentSubmittedThesis(UUID studentId) {
        return thesisRepository.findByStudentId(studentId).isPresent();
    }

    @Override
    public List<ThesisDto> findThesesAwaitingReview() {
        return thesisRepository.findAll().stream()
                .filter(thesis -> thesis.getStatus() == ThesisStatus.WAITING_FOR_REVIEW)
                .map(thesisMapper::thesisToThesisDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ThesisDto> findThesesAwaitingDefense() {
        return thesisRepository.findAll().stream()
                .filter(thesis -> thesis.getStatus() == ThesisStatus.WAITING_FOR_DEFENSE)
                .map(thesisMapper::thesisToThesisDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ThesisDto proceedToDefense(UUID thesisId) {
        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new ThesisNotFoundException("Thesis not found with id: " + thesisId));

        // Check if thesis has been accepted by review
        if (!canStudentProceedToDefense(thesisId)) {
            throw new IllegalStateException("Thesis cannot proceed to defense - review not accepted or not reviewed yet");
        }

        thesis.setStatus(ThesisStatus.WAITING_FOR_DEFENSE);
        Thesis updatedThesis = thesisRepository.save(thesis);
        return thesisMapper.thesisToThesisDto(updatedThesis);
    }

    @Override
    public boolean canStudentProceedToDefense(UUID thesisId) {
        // For now, we'll implement basic logic here
        // In a more sophisticated approach, we'd inject ReviewService
        Thesis thesis = thesisRepository.findById(thesisId).orElse(null);
        if (thesis == null) {
            return false;
        }
        
        // Simple check: if thesis has PDF and status allows progression
        return thesis.getPdfPath() != null && 
               (thesis.getStatus() == ThesisStatus.WAITING_FOR_REVIEW || 
                thesis.getStatus() == ThesisStatus.WAITING_FOR_DEFENSE);
    }

    private boolean isPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        return "application/pdf".equals(contentType);
    }
}
