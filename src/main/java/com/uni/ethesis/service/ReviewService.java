package com.uni.ethesis.service;

import java.util.List;
import java.util.UUID;

import com.uni.ethesis.data.dto.ReviewDto;
import com.uni.ethesis.enums.ReviewConclusion;

public interface ReviewService {
    
    // Basic CRUD operations
    ReviewDto createReview(ReviewDto reviewDto);
    ReviewDto getReviewById(UUID id);
    ReviewDto getReviewByThesisId(UUID thesisId);
    List<ReviewDto> getAllReviews();
    List<ReviewDto> getReviewsByTeacherId(UUID teacherId);
    ReviewDto updateReview(UUID id, ReviewDto reviewDto);
    void deleteReview(UUID id);
    
    // Core workflow methods
    ReviewDto submitReview(UUID thesisId, UUID teacherId, String content, ReviewConclusion conclusion);
    boolean hasThesisBeenReviewed(UUID thesisId);
    ReviewDto getLatestReviewForThesis(UUID thesisId);
    
    // Status management methods
    boolean canProceedToDefense(UUID thesisId);
    void markThesisReadyForDefense(UUID thesisId);
    
    // Statistics methods based on requirements
    long countReviewsByConclusion(ReviewConclusion conclusion);
    long countNegativeReviews();
    
    // Query methods
    List<ReviewDto> getReviewsByConclusion(ReviewConclusion conclusion);
    List<ReviewDto> getThesesAwaitingReview();
}
