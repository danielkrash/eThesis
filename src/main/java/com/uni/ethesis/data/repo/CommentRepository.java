package com.uni.ethesis.data.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uni.ethesis.data.entities.Comment;

public interface CommentRepository extends JpaRepository<Comment, UUID>, JpaSpecificationExecutor<Comment> {
    
    /**
     * Find all comments for a specific review, ordered by creation time ascending (oldest first)
     */
    @Query("SELECT c FROM Comment c WHERE c.review.id = :reviewId ORDER BY c.createdAt ASC")
    List<Comment> findByReviewIdOrderByCreatedAtAsc(@Param("reviewId") UUID reviewId);
    
    /**
     * Find all comments for a specific review, ordered by creation time descending (newest first)
     */
    @Query("SELECT c FROM Comment c WHERE c.review.id = :reviewId ORDER BY c.createdAt DESC")
    List<Comment> findByReviewIdOrderByCreatedAtDesc(@Param("reviewId") UUID reviewId);
    
    /**
     * Find all comments by a specific user
     */
    @Query("SELECT c FROM Comment c WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Comment> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);
    
    /**
     * Count comments for a specific review
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.review.id = :reviewId")
    long countByReviewId(@Param("reviewId") UUID reviewId);
}