package com.uni.ethesis.data.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uni.ethesis.data.entities.DefenseSessionProfessor;
import com.uni.ethesis.utils.DefenseSessionProfessorKey;

public interface DefenseSessionProfessorRepository extends JpaRepository<DefenseSessionProfessor, DefenseSessionProfessorKey>, JpaSpecificationExecutor<DefenseSessionProfessor> {
    
    /**
     * Find all professors for a specific defense session
     */
    @Query("SELECT dsp FROM DefenseSessionProfessor dsp WHERE dsp.defenseSession.id = :defenseSessionId")
    List<DefenseSessionProfessor> findByDefenseSessionId(@Param("defenseSessionId") UUID defenseSessionId);
    
    /**
     * Find all defense sessions for a specific professor
     */
    @Query("SELECT dsp FROM DefenseSessionProfessor dsp WHERE dsp.professor.id = :professorId ORDER BY dsp.defenseSession.dateAndTime DESC")
    List<DefenseSessionProfessor> findByProfessorId(@Param("professorId") UUID professorId);
    
    /**
     * Find a specific professor in a specific defense session
     */
    @Query("SELECT dsp FROM DefenseSessionProfessor dsp WHERE dsp.defenseSession.id = :defenseSessionId AND dsp.professor.id = :professorId")
    Optional<DefenseSessionProfessor> findByDefenseSessionIdAndProfessorId(@Param("defenseSessionId") UUID defenseSessionId, @Param("professorId") UUID professorId);
    
    /**
     * Check if a professor exists in a specific defense session
     */
    @Query("SELECT COUNT(dsp) > 0 FROM DefenseSessionProfessor dsp WHERE dsp.defenseSession.id = :defenseSessionId AND dsp.professor.id = :professorId")
    boolean existsByDefenseSessionIdAndProfessorId(@Param("defenseSessionId") UUID defenseSessionId, @Param("professorId") UUID professorId);
    
    /**
     * Find all professors who have graded (grade is not null) for a specific defense session
     */
    @Query("SELECT dsp FROM DefenseSessionProfessor dsp WHERE dsp.defenseSession.id = :defenseSessionId AND dsp.grade IS NOT NULL")
    List<DefenseSessionProfessor> findGradedByDefenseSessionId(@Param("defenseSessionId") UUID defenseSessionId);
    
    /**
     * Find all professors who haven't graded (grade is null) for a specific defense session
     */
    @Query("SELECT dsp FROM DefenseSessionProfessor dsp WHERE dsp.defenseSession.id = :defenseSessionId AND dsp.grade IS NULL")
    List<DefenseSessionProfessor> findNotGradedByDefenseSessionId(@Param("defenseSessionId") UUID defenseSessionId);
    
    /**
     * Count professors for a specific defense session
     */
    @Query("SELECT COUNT(dsp) FROM DefenseSessionProfessor dsp WHERE dsp.defenseSession.id = :defenseSessionId")
    long countByDefenseSessionId(@Param("defenseSessionId") UUID defenseSessionId);
    
    /**
     * Get average grade for a specific defense session
     */
    @Query("SELECT AVG(dsp.grade) FROM DefenseSessionProfessor dsp WHERE dsp.defenseSession.id = :defenseSessionId AND dsp.grade IS NOT NULL")
    Double getAverageGradeByDefenseSessionId(@Param("defenseSessionId") UUID defenseSessionId);
}
