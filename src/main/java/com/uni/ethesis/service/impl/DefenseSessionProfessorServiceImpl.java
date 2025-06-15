package com.uni.ethesis.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uni.ethesis.data.dto.DefenseSessionProfessorDto;
import com.uni.ethesis.data.entities.DefenseSession;
import com.uni.ethesis.data.entities.DefenseSessionProfessor;
import com.uni.ethesis.data.entities.Teacher;
import com.uni.ethesis.data.repo.DefenseSessionProfessorRepository;
import com.uni.ethesis.data.repo.DefenseSessionRepository;
import com.uni.ethesis.data.repo.TeacherRepository;
import com.uni.ethesis.exceptions.DefenseSessionNotFoundException;
import com.uni.ethesis.exceptions.DefenseSessionProfessorNotFoundException;
import com.uni.ethesis.exceptions.ProfessorAlreadyAssignedException;
import com.uni.ethesis.exceptions.TeacherNotFoundException;
import com.uni.ethesis.service.DefenseSessionProfessorService;
import com.uni.ethesis.utils.DefenseSessionProfessorKey;
import com.uni.ethesis.utils.mappers.DefenseSessionProfessorMapper;

@Service
public class DefenseSessionProfessorServiceImpl implements DefenseSessionProfessorService {

    private final DefenseSessionProfessorRepository defenseSessionProfessorRepository;
    private final DefenseSessionRepository defenseSessionRepository;
    private final TeacherRepository teacherRepository;
    private final DefenseSessionProfessorMapper defenseSessionProfessorMapper;

    @Autowired
    public DefenseSessionProfessorServiceImpl(DefenseSessionProfessorRepository defenseSessionProfessorRepository,
                                              DefenseSessionRepository defenseSessionRepository,
                                              TeacherRepository teacherRepository,
                                              DefenseSessionProfessorMapper defenseSessionProfessorMapper) {
        this.defenseSessionProfessorRepository = defenseSessionProfessorRepository;
        this.defenseSessionRepository = defenseSessionRepository;
        this.teacherRepository = teacherRepository;
        this.defenseSessionProfessorMapper = defenseSessionProfessorMapper;
    }

    @Override
    @Transactional
    public DefenseSessionProfessorDto addProfessorToDefenseSession(UUID defenseSessionId, UUID professorId) {
        // Validate that the defense session exists
        DefenseSession defenseSession = defenseSessionRepository.findById(defenseSessionId)
                .orElseThrow(() -> new DefenseSessionNotFoundException("Defense session not found with id: " + defenseSessionId));

        // Validate that the teacher exists
        Teacher professor = teacherRepository.findById(professorId)
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id: " + professorId));

        // Check if the professor is already assigned to this defense session
        if (isProfessorAssignedToDefenseSession(defenseSessionId, professorId)) {
            throw new ProfessorAlreadyAssignedException("Professor is already assigned to this defense session");
        }

        // Create the composite key
        DefenseSessionProfessorKey key = new DefenseSessionProfessorKey(defenseSessionId, professorId);

        // Create the DefenseSessionProfessor entity
        DefenseSessionProfessor defenseSessionProfessor = DefenseSessionProfessor.builder()
                .id(key)
                .defenseSession(defenseSession)
                .professor(professor)
                .grade(null) // Initially no grade
                .thoughts(null) // Initially no thoughts
                .build();

        DefenseSessionProfessor saved = defenseSessionProfessorRepository.save(defenseSessionProfessor);
        return defenseSessionProfessorMapper.defenseSessionProfessorToDto(saved);
    }

    @Override
    @Transactional
    public void removeProfessorFromDefenseSession(UUID defenseSessionId, UUID professorId) {
        DefenseSessionProfessorKey key = new DefenseSessionProfessorKey(defenseSessionId, professorId);
        
        DefenseSessionProfessor defenseSessionProfessor = defenseSessionProfessorRepository.findById(key)
                .orElseThrow(() -> new DefenseSessionProfessorNotFoundException(
                        "Professor assignment not found for defense session: " + defenseSessionId + " and professor: " + professorId));

        defenseSessionProfessorRepository.delete(defenseSessionProfessor);
    }

    @Override
    @Transactional
    public DefenseSessionProfessorDto updateProfessorEvaluation(UUID defenseSessionId, UUID professorId, 
                                                                Integer grade, String thoughts) {
        DefenseSessionProfessor defenseSessionProfessor = defenseSessionProfessorRepository
                .findByDefenseSessionIdAndProfessorId(defenseSessionId, professorId)
                .orElseThrow(() -> new DefenseSessionProfessorNotFoundException(
                        "Professor assignment not found for defense session: " + defenseSessionId + " and professor: " + professorId));

        // Update grade and thoughts
        defenseSessionProfessor.setGrade(grade);
        defenseSessionProfessor.setThoughts(thoughts);

        DefenseSessionProfessor updated = defenseSessionProfessorRepository.save(defenseSessionProfessor);
        return defenseSessionProfessorMapper.defenseSessionProfessorToDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DefenseSessionProfessorDto> getProfessorsByDefenseSession(UUID defenseSessionId) {
        // Validate that the defense session exists
        if (!defenseSessionRepository.existsById(defenseSessionId)) {
            throw new DefenseSessionNotFoundException("Defense session not found with id: " + defenseSessionId);
        }

        List<DefenseSessionProfessor> professors = defenseSessionProfessorRepository.findByDefenseSessionId(defenseSessionId);
        return defenseSessionProfessorMapper.defenseSessionProfessorsToDto(professors);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DefenseSessionProfessorDto> getDefenseSessionsByProfessor(UUID professorId) {
        // Validate that the teacher exists
        if (!teacherRepository.existsById(professorId)) {
            throw new TeacherNotFoundException("Teacher not found with id: " + professorId);
        }

        List<DefenseSessionProfessor> defenseSessions = defenseSessionProfessorRepository.findByProfessorId(professorId);
        return defenseSessionProfessorMapper.defenseSessionProfessorsToDto(defenseSessions);
    }

    @Override
    @Transactional(readOnly = true)
    public DefenseSessionProfessorDto getProfessorEvaluation(UUID defenseSessionId, UUID professorId) {
        DefenseSessionProfessor defenseSessionProfessor = defenseSessionProfessorRepository
                .findByDefenseSessionIdAndProfessorId(defenseSessionId, professorId)
                .orElseThrow(() -> new DefenseSessionProfessorNotFoundException(
                        "Professor assignment not found for defense session: " + defenseSessionId + " and professor: " + professorId));

        return defenseSessionProfessorMapper.defenseSessionProfessorToDto(defenseSessionProfessor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DefenseSessionProfessorDto> getGradedProfessorsByDefenseSession(UUID defenseSessionId) {
        // Validate that the defense session exists
        if (!defenseSessionRepository.existsById(defenseSessionId)) {
            throw new DefenseSessionNotFoundException("Defense session not found with id: " + defenseSessionId);
        }

        List<DefenseSessionProfessor> gradedProfessors = defenseSessionProfessorRepository.findGradedByDefenseSessionId(defenseSessionId);
        return defenseSessionProfessorMapper.defenseSessionProfessorsToDto(gradedProfessors);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DefenseSessionProfessorDto> getNotGradedProfessorsByDefenseSession(UUID defenseSessionId) {
        // Validate that the defense session exists
        if (!defenseSessionRepository.existsById(defenseSessionId)) {
            throw new DefenseSessionNotFoundException("Defense session not found with id: " + defenseSessionId);
        }

        List<DefenseSessionProfessor> notGradedProfessors = defenseSessionProfessorRepository.findNotGradedByDefenseSessionId(defenseSessionId);
        return defenseSessionProfessorMapper.defenseSessionProfessorsToDto(notGradedProfessors);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageGradeForDefenseSession(UUID defenseSessionId) {
        // Validate that the defense session exists
        if (!defenseSessionRepository.existsById(defenseSessionId)) {
            throw new DefenseSessionNotFoundException("Defense session not found with id: " + defenseSessionId);
        }

        return defenseSessionProfessorRepository.getAverageGradeByDefenseSessionId(defenseSessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProfessorAssignedToDefenseSession(UUID defenseSessionId, UUID professorId) {
        return defenseSessionProfessorRepository.existsByDefenseSessionIdAndProfessorId(defenseSessionId, professorId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean areAllProfessorsGraded(UUID defenseSessionId) {
        long totalProfessors = getProfessorCountForDefenseSession(defenseSessionId);
        long gradedProfessors = defenseSessionProfessorRepository.findGradedByDefenseSessionId(defenseSessionId).size();
        
        return totalProfessors > 0 && totalProfessors == gradedProfessors;
    }

    @Override
    @Transactional(readOnly = true)
    public long getProfessorCountForDefenseSession(UUID defenseSessionId) {
        // Validate that the defense session exists
        if (!defenseSessionRepository.existsById(defenseSessionId)) {
            throw new DefenseSessionNotFoundException("Defense session not found with id: " + defenseSessionId);
        }

        return defenseSessionProfessorRepository.countByDefenseSessionId(defenseSessionId);
    }
}
