package com.uni.ethesis.service.impl;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uni.ethesis.data.dto.DefenseDto;
import com.uni.ethesis.data.entities.Defense;
import com.uni.ethesis.data.repo.DefenseRepository;
import com.uni.ethesis.data.repo.ThesisRepository;
import com.uni.ethesis.exceptions.DefenseNotFoundException;
import com.uni.ethesis.service.DefenseService;
import com.uni.ethesis.utils.mappers.DefenseMapper;

@Service
public class DefenseServiceImpl implements DefenseService {

    private final DefenseRepository defenseRepository;
    private final ThesisRepository thesisRepository;
    private final DefenseMapper defenseMapper;

    private static final BigDecimal MIN_PASSING_GRADE = BigDecimal.valueOf(3.0);

    @Autowired
    public DefenseServiceImpl(DefenseRepository defenseRepository,
                              ThesisRepository thesisRepository,
                              DefenseMapper defenseMapper) {
        this.defenseRepository = defenseRepository;
        this.thesisRepository = thesisRepository;
        this.defenseMapper = defenseMapper;
    }

    @Override
    @Transactional
    public DefenseDto createDefense(DefenseDto defenseDto) {
        Defense defense = defenseMapper.defenseDtoToDefense(defenseDto);
        Defense savedDefense = defenseRepository.save(defense);
        return defenseMapper.defenseToDefenseDto(savedDefense);
    }

    @Override
    public DefenseDto getDefenseById(UUID id) {
        Defense defense = defenseRepository.findById(id)
                .orElseThrow(() -> new DefenseNotFoundException("Defense not found with id: " + id));
        return defenseMapper.defenseToDefenseDto(defense);
    }

    @Override
    public List<DefenseDto> getAllDefenses() {
        return defenseRepository.findAll().stream()
                .map(defenseMapper::defenseToDefenseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DefenseDto> getDefensesByDate(Date date) {
        return defenseRepository.findByDate(date).stream()
                .map(defenseMapper::defenseToDefenseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DefenseDto> getDefensesByDateRange(Date startDate, Date endDate) {
        return defenseRepository.findByDateBetween(startDate, endDate).stream()
                .map(defenseMapper::defenseToDefenseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DefenseDto updateDefense(UUID id, DefenseDto defenseDto) {
        Defense existingDefense = defenseRepository.findById(id)
                .orElseThrow(() -> new DefenseNotFoundException("Defense not found with id: " + id));

        if (defenseDto.getLocation() != null) {
            existingDefense.setLocation(defenseDto.getLocation());
        }
        if (defenseDto.getDate() != null) {
            existingDefense.setDate(defenseDto.getDate());
        }

        Defense updatedDefense = defenseRepository.save(existingDefense);
        return defenseMapper.defenseToDefenseDto(updatedDefense);
    }

    @Override
    @Transactional
    public void deleteDefense(UUID id) {
        if (!defenseRepository.existsById(id)) {
            throw new DefenseNotFoundException("Defense not found with id: " + id);
        }
        defenseRepository.deleteById(id);
    }

    @Override
    public long countSuccessfulDefensesByTeacher(UUID teacherId) {
        return thesisRepository.countSuccessfulDefensesByTeacher(teacherId, MIN_PASSING_GRADE);
    }

    @Override
    public List<DefenseDto> getDefensesByLocation(String location) {
        return defenseRepository.findByLocationIgnoreCase(location).stream()
                .map(defenseMapper::defenseToDefenseDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countDefensesInPeriod(Date startDate, Date endDate) {
        return defenseRepository.countDefensesInPeriod(startDate, endDate);
    }
}
