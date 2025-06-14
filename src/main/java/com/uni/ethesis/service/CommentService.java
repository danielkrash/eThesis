package com.uni.ethesis.service;

import java.util.List;
import java.util.UUID;

import com.uni.ethesis.data.dto.CommentDto;

public interface CommentService {
    
    /**
     * Create a new comment for a review by a user (student or teacher)
     * @param reviewId The ID of the review to comment on
     * @param userId The ID of the user creating the comment
     * @param content The content of the comment
     * @return The created comment DTO
     */
    CommentDto createComment(UUID reviewId, UUID userId, String content);
    
    /**
     * Get all comments for a specific review, sorted by creation time (oldest first)
     * @param reviewId The ID of the review
     * @return List of comments sorted by creation time
     */
    List<CommentDto> getCommentsByReviewId(UUID reviewId);
    
    /**
     * Get all comments for a specific review, sorted by creation time (newest first)
     * @param reviewId The ID of the review
     * @return List of comments sorted by creation time (newest first)
     */
    List<CommentDto> getCommentsByReviewIdNewestFirst(UUID reviewId);
    
    /**
     * Get a comment by its ID
     * @param commentId The ID of the comment
     * @return The comment DTO
     */
    CommentDto getCommentById(UUID commentId);
    
    /**
     * Update an existing comment (only by the original author)
     * @param commentId The ID of the comment to update
     * @param userId The ID of the user attempting to update (must be the original author)
     * @param newContent The new content for the comment
     * @return The updated comment DTO
     */
    CommentDto updateComment(UUID commentId, UUID userId, String newContent);
    
    /**
     * Delete a comment by its ID (only by the original author)
     * @param commentId The ID of the comment to delete
     * @param userId The ID of the user attempting to delete (must be the original author)
     */
    void deleteComment(UUID commentId, UUID userId);
    
    /**
     * Get all comments made by a specific user
     * @param userId The ID of the user
     * @return List of comments by the user
     */
    List<CommentDto> getCommentsByUserId(UUID userId);
    
    /**
     * Check if a user can comment on a review (business logic validation)
     * @param reviewId The ID of the review
     * @param userId The ID of the user
     * @return true if the user can comment, false otherwise
     */
    boolean canUserCommentOnReview(UUID reviewId, UUID userId);
}
