package com.uni.ethesis.service.impl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uni.ethesis.data.dto.DefenseSessionDto;
import com.uni.ethesis.data.entities.Defense;
import com.uni.ethesis.data.entities.DefenseSession;
import com.uni.ethesis.data.entities.Thesis;
import com.uni.ethesis.data.repo.DefenseRepository;
import com.uni.ethesis.data.repo.DefenseSessionRepository;
import com.uni.ethesis.data.repo.ThesisRepository;
import com.uni.ethesis.enums.ThesisStatus;
import com.uni.ethesis.exceptions.DefenseNotFoundException;
import com.uni.ethesis.exceptions.DefenseSessionNotFoundException;
import com.uni.ethesis.exceptions.ThesisNotFoundException;
import com.uni.ethesis.service.DefenseSessionService;
import com.uni.ethesis.utils.mappers.DefenseSessionMapper;

@Service
public class DefenseSessionServiceImpl implements DefenseSessionService {

    private final DefenseSessionRepository defenseSessionRepository;
    private final DefenseRepository defenseRepository;
    private final ThesisRepository thesisRepository;
    private final DefenseSessionMapper defenseSessionMapper;

    private static final BigDecimal MIN_PASSING_GRADE = BigDecimal.valueOf(3.0);

    @Autowired
    public DefenseSessionServiceImpl(DefenseSessionRepository defenseSessionRepository,
                                     DefenseRepository defenseRepository,
                                     ThesisRepository thesisRepository,
                                     DefenseSessionMapper defenseSessionMapper) {
        this.defenseSessionRepository = defenseSessionRepository;
        this.defenseRepository = defenseRepository;
        this.thesisRepository = thesisRepository;
        this.defenseSessionMapper = defenseSessionMapper;
    }

    @Override
    @Transactional
    public DefenseSessionDto createDefenseSession(DefenseSessionDto defenseSessionDto) {
        DefenseSession defenseSession = defenseSessionMapper.defenseSessionDtoToDefenseSession(defenseSessionDto);
        
        // Set thesis
        if (defenseSessionDto.getThesisId() != null) {
            Thesis thesis = thesisRepository.findById(defenseSessionDto.getThesisId())
                    .orElseThrow(() -> new ThesisNotFoundException("Thesis not found with id: " + defenseSessionDto.getThesisId()));
            defenseSession.setThesis(thesis);
        }
        
        // Set defense
        if (defenseSessionDto.getDefenseId() != null) {
            Defense defense = defenseRepository.findById(defenseSessionDto.getDefenseId())
                    .orElseThrow(() -> new DefenseNotFoundException("Defense not found with id: " + defenseSessionDto.getDefenseId()));
            defenseSession.setDefense(defense);
        }

        DefenseSession savedDefenseSession = defenseSessionRepository.save(defenseSession);
        return defenseSessionMapper.defenseSessionToDefenseSessionDto(savedDefenseSession);
    }

    @Override
    public DefenseSessionDto getDefenseSessionById(UUID id) {
        DefenseSession defenseSession = defenseSessionRepository.findById(id)
                .orElseThrow(() -> new DefenseSessionNotFoundException("Defense session not found with id: " + id));
        return defenseSessionMapper.defenseSessionToDefenseSessionDto(defenseSession);
    }

    @Override
    public DefenseSessionDto getDefenseSessionByThesisId(UUID thesisId) {
        DefenseSession defenseSession = defenseSessionRepository.findByThesisId(thesisId)
                .orElseThrow(() -> new DefenseSessionNotFoundException("Defense session not found for thesis id: " + thesisId));
        return defenseSessionMapper.defenseSessionToDefenseSessionDto(defenseSession);
    }

    @Override
    public List<DefenseSessionDto> getDefenseSessionsByDefenseId(UUID defenseId) {
        return defenseSessionRepository.findByDefenseId(defenseId).stream()
                .map(defenseSessionMapper::defenseSessionToDefenseSessionDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DefenseSessionDto> getAllDefenseSessions() {
        return defenseSessionRepository.findAll().stream()
                .map(defenseSessionMapper::defenseSessionToDefenseSessionDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DefenseSessionDto updateDefenseSession(UUID id, DefenseSessionDto defenseSessionDto) {
        DefenseSession existingDefenseSession = defenseSessionRepository.findById(id)
                .orElseThrow(() -> new DefenseSessionNotFoundException("Defense session not found with id: " + id));

        if (defenseSessionDto.getDateAndTime() != null) {
            existingDefenseSession.setDateAndTime(defenseSessionDto.getDateAndTime());
        }
        if (defenseSessionDto.getNotes() != null) {
            existingDefenseSession.setNotes(defenseSessionDto.getNotes());
        }

        DefenseSession updatedDefenseSession = defenseSessionRepository.save(existingDefenseSession);
        return defenseSessionMapper.defenseSessionToDefenseSessionDto(updatedDefenseSession);
    }

    @Override
    @Transactional
    public void deleteDefenseSession(UUID id) {
        if (!defenseSessionRepository.existsById(id)) {
            throw new DefenseSessionNotFoundException("Defense session not found with id: " + id);
        }
        defenseSessionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public DefenseSessionDto scheduleDefenseForThesis(UUID thesisId, UUID defenseId, OffsetDateTime dateTime, String notes) {
        // Validate thesis exists and is ready for defense
        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new ThesisNotFoundException("Thesis not found with id: " + thesisId));

        if (thesis.getStatus() != ThesisStatus.WAITING_FOR_DEFENSE) {
            throw new IllegalStateException("Thesis is not ready for defense. Current status: " + thesis.getStatus());
        }

        // Validate defense exists
        Defense defense = defenseRepository.findById(defenseId)
                .orElseThrow(() -> new DefenseNotFoundException("Defense not found with id: " + defenseId));

        // Create defense session
        DefenseSession defenseSession = DefenseSession.builder()
                .thesis(thesis)
                .defense(defense)
                .dateAndTime(dateTime)
                .notes(notes)
                .build();

        DefenseSession savedDefenseSession = defenseSessionRepository.save(defenseSession);
        return defenseSessionMapper.defenseSessionToDefenseSessionDto(savedDefenseSession);
    }

    @Override
    @Transactional
    public DefenseSessionDto recordDefenseGrade(UUID defenseSessionId, BigDecimal finalGrade) {
        DefenseSession defenseSession = defenseSessionRepository.findById(defenseSessionId)
                .orElseThrow(() -> new DefenseSessionNotFoundException("Defense session not found with id: " + defenseSessionId));

        // Update thesis with final grade and status
        Thesis thesis = defenseSession.getThesis();
        thesis.setFinalGrade(finalGrade);
        
        if (finalGrade.compareTo(MIN_PASSING_GRADE) >= 0) {
            thesis.setStatus(ThesisStatus.DEFENDED);
        } else {
            thesis.setStatus(ThesisStatus.FAILED);
        }
        
        thesisRepository.save(thesis);
        
        return defenseSessionMapper.defenseSessionToDefenseSessionDto(defenseSession);
    }

    @Override
    public boolean isThesisScheduledForDefense(UUID thesisId) {
        return defenseSessionRepository.findByThesisId(thesisId).isPresent();
    }

    @Override
    public List<DefenseSessionDto> getDefenseSessionsByTeacher(UUID teacherId) {
        return defenseSessionRepository.findByTeacherId(teacherId).stream()
                .map(defenseSessionMapper::defenseSessionToDefenseSessionDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DefenseSessionDto> getDefenseSessionsInPeriod(OffsetDateTime startDate, OffsetDateTime endDate) {
        return defenseSessionRepository.findByDateAndTimeBetween(startDate, endDate).stream()
                .map(defenseSessionMapper::defenseSessionToDefenseSessionDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countStudentsDefendedInPeriod(OffsetDateTime startDate, OffsetDateTime endDate) {
        return defenseSessionRepository.countStudentsDefendedInPeriod(startDate, endDate);
    }

    @Override
    public double getAverageAttendanceInPeriod(OffsetDateTime startDate, OffsetDateTime endDate) {
        Double average = defenseSessionRepository.getAverageAttendanceInPeriod(startDate, endDate);
        return average != null ? average : 0.0;
    }
}
