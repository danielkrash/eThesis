package com.uni.ethesis.data.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uni.ethesis.data.entities.Review;
import com.uni.ethesis.enums.ReviewConclusion;

public interface ReviewRepository extends JpaRepository<Review, UUID>, JpaSpecificationExecutor<Review> {
    
    // Find review by thesis ID
    Optional<Review> findByThesisId(UUID thesisId);
    
    // Find all reviews by teacher
    List<Review> findByTeacherId(UUID teacherId);
    
    // Find reviews by conclusion
    List<Review> findByConclusion(ReviewConclusion conclusion);
    
    // Count reviews with negative conclusion
    long countByConclusion(ReviewConclusion conclusion);
    
    // Check if thesis has been reviewed
    boolean existsByThesisId(UUID thesisId);
    
    // Find latest review for thesis (in case of resubmissions)
    @Query("SELECT r FROM Review r WHERE r.thesis.id = :thesisId ORDER BY r.createdAt DESC")
    Optional<Review> findLatestByThesisId(@Param("thesisId") UUID thesisId);
}