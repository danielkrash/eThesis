package com.uni.ethesis.service;

import java.util.List;
import java.util.UUID;

import com.uni.ethesis.data.dto.DefenseSessionProfessorDto;

public interface DefenseSessionProfessorService {
    
    /**
     * Add a professor to a defense session
     * @param defenseSessionId The ID of the defense session
     * @param professorId The ID of the professor (Teacher)
     * @return The created DefenseSessionProfessorDto
     */
    DefenseSessionProfessorDto addProfessorToDefenseSession(UUID defenseSessionId, UUID professorId);
    
    /**
     * Remove a professor from a defense session
     * @param defenseSessionId The ID of the defense session
     * @param professorId The ID of the professor
     */
    void removeProfessorFromDefenseSession(UUID defenseSessionId, UUID professorId);
    
    /**
     * Update professor's grade and thoughts for a defense session
     * @param defenseSessionId The ID of the defense session
     * @param professorId The ID of the professor
     * @param grade The grade to assign (0-100)
     * @param thoughts The professor's thoughts/comments
     * @return The updated DefenseSessionProfessorDto
     */
    DefenseSessionProfessorDto updateProfessorEvaluation(UUID defenseSessionId, UUID professorId, Integer grade, String thoughts);
    
    /**
     * Get all professors for a specific defense session
     * @param defenseSessionId The ID of the defense session
     * @return List of professors in the defense session
     */
    List<DefenseSessionProfessorDto> getProfessorsByDefenseSession(UUID defenseSessionId);
    
    /**
     * Get all defense sessions for a specific professor
     * @param professorId The ID of the professor
     * @return List of defense sessions for the professor
     */
    List<DefenseSessionProfessorDto> getDefenseSessionsByProfessor(UUID professorId);
    
    /**
     * Get a specific professor's evaluation for a defense session
     * @param defenseSessionId The ID of the defense session
     * @param professorId The ID of the professor
     * @return The professor's evaluation for the session
     */
    DefenseSessionProfessorDto getProfessorEvaluation(UUID defenseSessionId, UUID professorId);
    
    /**
     * Get professors who have submitted grades for a defense session
     * @param defenseSessionId The ID of the defense session
     * @return List of professors who have graded
     */
    List<DefenseSessionProfessorDto> getGradedProfessorsByDefenseSession(UUID defenseSessionId);
    
    /**
     * Get professors who haven't submitted grades for a defense session
     * @param defenseSessionId The ID of the defense session
     * @return List of professors who haven't graded yet
     */
    List<DefenseSessionProfessorDto> getNotGradedProfessorsByDefenseSession(UUID defenseSessionId);
    
    /**
     * Get the average grade for a defense session
     * @param defenseSessionId The ID of the defense session
     * @return The average grade, or null if no grades have been submitted
     */
    Double getAverageGradeForDefenseSession(UUID defenseSessionId);
    
    /**
     * Check if a professor is already assigned to a defense session
     * @param defenseSessionId The ID of the defense session
     * @param professorId The ID of the professor
     * @return true if the professor is assigned, false otherwise
     */
    boolean isProfessorAssignedToDefenseSession(UUID defenseSessionId, UUID professorId);
    
    /**
     * Check if all professors have submitted their grades for a defense session
     * @param defenseSessionId The ID of the defense session
     * @return true if all professors have graded, false otherwise
     */
    boolean areAllProfessorsGraded(UUID defenseSessionId);
    
    /**
     * Get count of professors assigned to a defense session
     * @param defenseSessionId The ID of the defense session
     * @return The number of professors assigned
     */
    long getProfessorCountForDefenseSession(UUID defenseSessionId);
}
