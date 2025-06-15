package com.uni.ethesis.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uni.ethesis.data.dto.CommentDto;
import com.uni.ethesis.data.entities.Comment;
import com.uni.ethesis.data.entities.Review;
import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.data.repo.CommentRepository;
import com.uni.ethesis.data.repo.ReviewRepository;
import com.uni.ethesis.data.repo.TeacherRepository;
import com.uni.ethesis.data.repo.UserRepository;
import com.uni.ethesis.exceptions.CommentNotFoundException;
import com.uni.ethesis.exceptions.ReviewNotFoundException;
import com.uni.ethesis.exceptions.UnauthorizedCommentException;
import com.uni.ethesis.exceptions.UserNotFoundException;
import com.uni.ethesis.service.CommentService;
import com.uni.ethesis.utils.mappers.CommentMapper;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final CommentMapper commentMapper;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                              ReviewRepository reviewRepository,
                              UserRepository userRepository,
                              TeacherRepository teacherRepository,
                              CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.commentMapper = commentMapper;
    }

    @Override
    @Transactional
    public CommentDto createComment(UUID reviewId, UUID userId, String content) {
        // Validate input
        if (reviewId == null) {
            throw new IllegalArgumentException("Review ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        // Find the review
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));

        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Check if user can comment on this review
        if (!canUserCommentOnReview(reviewId, userId)) {
            throw new UnauthorizedCommentException("User is not authorized to comment on this review");
        }

        // Create and save the comment
        Comment comment = Comment.builder()
                .content(content.trim())
                .review(review)
                .user(user)
                .build();

        Comment savedComment = commentRepository.save(comment);
        CommentDto commentDto = commentMapper.commentToCommentDto(savedComment);
        
        // Set the correct user role since MapStruct can't handle repository calls
        commentDto.setUserRole(determineUserRole(user));
        
        return commentDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByReviewId(UUID reviewId) {
        if (reviewId == null) {
            throw new IllegalArgumentException("Review ID cannot be null");
        }
        
        // Validate that the review exists
        if (!reviewRepository.existsById(reviewId)) {
            throw new ReviewNotFoundException("Review not found with id: " + reviewId);
        }

        List<Comment> comments = commentRepository.findByReviewIdOrderByCreatedAtAsc(reviewId);
        List<CommentDto> commentDtos = commentMapper.commentsToCommentDtos(comments);
        
        // Set the correct user roles
        commentDtos.forEach(dto -> {
            User user = userRepository.findById(dto.getUserId()).orElse(null);
            if (user != null) {
                dto.setUserRole(determineUserRole(user));
            }
        });
        
        return commentDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByReviewIdNewestFirst(UUID reviewId) {
        if (reviewId == null) {
            throw new IllegalArgumentException("Review ID cannot be null");
        }
        
        // Validate that the review exists
        if (!reviewRepository.existsById(reviewId)) {
            throw new ReviewNotFoundException("Review not found with id: " + reviewId);
        }

        List<Comment> comments = commentRepository.findByReviewIdOrderByCreatedAtDesc(reviewId);
        List<CommentDto> commentDtos = commentMapper.commentsToCommentDtos(comments);
        
        // Set the correct user roles
        commentDtos.forEach(dto -> {
            User user = userRepository.findById(dto.getUserId()).orElse(null);
            if (user != null) {
                dto.setUserRole(determineUserRole(user));
            }
        });
        
        return commentDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getCommentById(UUID commentId) {
        if (commentId == null) {
            throw new IllegalArgumentException("Comment ID cannot be null");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));

        CommentDto commentDto = commentMapper.commentToCommentDto(comment);
        commentDto.setUserRole(determineUserRole(comment.getUser()));
        
        return commentDto;
    }

    @Override
    @Transactional
    public CommentDto updateComment(UUID commentId, UUID userId, String newContent) {
        // Validate input
        if (commentId == null) {
            throw new IllegalArgumentException("Comment ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        // Find the comment
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));

        // Check if the user is the original author
        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedCommentException("Only the original author can update this comment");
        }

        // Update the comment
        comment.setContent(newContent.trim());
        Comment updatedComment = commentRepository.save(comment);

        CommentDto commentDto = commentMapper.commentToCommentDto(updatedComment);
        commentDto.setUserRole(determineUserRole(updatedComment.getUser()));
        
        return commentDto;
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId, UUID userId) {
        // Validate input
        if (commentId == null) {
            throw new IllegalArgumentException("Comment ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        // Find the comment
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));

        // Check if the user is the original author
        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedCommentException("Only the original author can delete this comment");
        }

        // Delete the comment
        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        List<Comment> comments = commentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<CommentDto> commentDtos = commentMapper.commentsToCommentDtos(comments);
        
        // Set the correct user roles
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            String userRole = determineUserRole(user);
            commentDtos.forEach(dto -> dto.setUserRole(userRole));
        }
        
        return commentDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserCommentOnReview(UUID reviewId, UUID userId) {
        // Basic validation
        if (reviewId == null || userId == null) {
            return false;
        }

        // Check if review and user exist using existsById for efficiency
        return reviewRepository.existsById(reviewId) && userRepository.existsById(userId);
        
        // Business logic: Both teachers and students can comment on reviews
        // Additional business rules can be added here if needed, such as:
        // - Only users from the same department can comment
        // - Only after certain thesis status
        // For now, we allow all authenticated users to comment if they exist
    }

    /**
     * Helper method to determine if a user is a teacher or student
     */
    private String determineUserRole(User user) {
        // Check if user is a teacher by checking if they have any teacher record
        return teacherRepository.findByUserId(user.getId()) != null ? "TEACHER" : "STUDENT";
    }
}
