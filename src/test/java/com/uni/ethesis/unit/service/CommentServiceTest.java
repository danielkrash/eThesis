package com.uni.ethesis.unit.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uni.ethesis.data.dto.CommentDto;
import com.uni.ethesis.data.entities.Comment;
import com.uni.ethesis.data.entities.Review;
import com.uni.ethesis.data.entities.Teacher;
import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.data.repo.CommentRepository;
import com.uni.ethesis.data.repo.ReviewRepository;
import com.uni.ethesis.data.repo.TeacherRepository;
import com.uni.ethesis.data.repo.UserRepository;
import com.uni.ethesis.exceptions.ReviewNotFoundException;
import com.uni.ethesis.exceptions.UserNotFoundException;
import com.uni.ethesis.service.impl.CommentServiceImpl;
import com.uni.ethesis.utils.mappers.CommentMapper;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    private UUID reviewId;
    private UUID userId;
    private UUID teacherId;
    private UUID commentId;
    private Review review;
    private User user;
    private User teacher;
    private Comment comment;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        reviewId = UUID.randomUUID();
        userId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        // Create test entities
        review = Review.builder()
                .id(reviewId)
                .content("Test review content")
                .build();

        user = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        teacher = User.builder()
                .id(teacherId)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .build();

        comment = Comment.builder()
                .id(commentId)
                .content("Test comment content")
                .review(review)
                .user(user)
                .build();

        commentDto = CommentDto.builder()
                .id(commentId)
                .content("Test comment content")
                .reviewId(reviewId)
                .userId(userId)
                .userFullName("John Doe")
                .userRole("STUDENT")
                .build();
    }

    @Test
    void createComment_Success() {
        // Given
        String content = "This is a test comment";
        
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reviewRepository.existsById(reviewId)).thenReturn(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.commentToCommentDto(comment)).thenReturn(commentDto);
        when(teacherRepository.findByUserId(userId)).thenReturn(null); // Student

        // When
        CommentDto result = commentService.createComment(reviewId, userId, content);

        // Then
        assertNotNull(result);
        assertEquals("STUDENT", result.getUserRole());
        assertEquals("John Doe", result.getUserFullName());
        
        verify(commentRepository).save(any(Comment.class));
        verify(commentMapper).commentToCommentDto(any(Comment.class));
    }

    @Test
    void createComment_TeacherRole() {
        // Given
        String content = "Teacher comment";
        Teacher teacherEntity = Teacher.builder().id(teacherId).user(teacher).build();
        
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(reviewRepository.existsById(reviewId)).thenReturn(true);
        when(userRepository.existsById(teacherId)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.commentToCommentDto(comment)).thenReturn(commentDto);
        when(teacherRepository.findByUserId(teacherId)).thenReturn(teacherEntity); // Teacher

        // When
        CommentDto result = commentService.createComment(reviewId, teacherId, content);

        // Then
        assertNotNull(result);
        assertEquals("TEACHER", result.getUserRole());
        
        verify(teacherRepository).findByUserId(teacherId);
    }

    @Test
    void createComment_ReviewNotFound() {
        // Given
        String content = "Test comment";
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ReviewNotFoundException.class, 
                () -> commentService.createComment(reviewId, userId, content));
    }

    @Test
    void createComment_UserNotFound() {
        // Given
        String content = "Test comment";
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, 
                () -> commentService.createComment(reviewId, userId, content));
    }

    @Test
    void getCommentsByReviewId_Success() {
        // Given
        List<Comment> comments = Arrays.asList(comment);
        List<CommentDto> commentDtos = Arrays.asList(commentDto);
        
        when(reviewRepository.existsById(reviewId)).thenReturn(true);
        when(commentRepository.findByReviewIdOrderByCreatedAtAsc(reviewId)).thenReturn(comments);
        when(commentMapper.commentsToCommentDtos(comments)).thenReturn(commentDtos);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(teacherRepository.findByUserId(userId)).thenReturn(null); // Student

        // When
        List<CommentDto> result = commentService.getCommentsByReviewId(reviewId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("STUDENT", result.get(0).getUserRole());
        
        verify(commentRepository).findByReviewIdOrderByCreatedAtAsc(reviewId);
    }

    @Test
    void getCommentsByReviewId_ReviewNotFound() {
        // Given
        when(reviewRepository.existsById(reviewId)).thenReturn(false);

        // When & Then
        assertThrows(ReviewNotFoundException.class, 
                () -> commentService.getCommentsByReviewId(reviewId));
    }

    @Test
    void canUserCommentOnReview_Success() {
        // Given
        when(reviewRepository.existsById(reviewId)).thenReturn(true);
        when(userRepository.existsById(userId)).thenReturn(true);

        // When
        boolean result = commentService.canUserCommentOnReview(reviewId, userId);

        // Then
        assertTrue(result);
    }

    @Test
    void canUserCommentOnReview_InvalidIds() {
        // When & Then
        assertFalse(commentService.canUserCommentOnReview(null, userId));
        assertFalse(commentService.canUserCommentOnReview(reviewId, null));
        assertFalse(commentService.canUserCommentOnReview(null, null));
    }
}
