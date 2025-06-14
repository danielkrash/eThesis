package com.uni.ethesis.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uni.ethesis.data.dto.ReviewDto;
import com.uni.ethesis.data.entities.Review;
import com.uni.ethesis.data.entities.Teacher;
import com.uni.ethesis.data.entities.Thesis;
import com.uni.ethesis.data.repo.ReviewRepository;
import com.uni.ethesis.data.repo.TeacherRepository;
import com.uni.ethesis.data.repo.ThesisRepository;
import com.uni.ethesis.enums.ReviewConclusion;
import com.uni.ethesis.enums.ThesisStatus;
import com.uni.ethesis.exceptions.ReviewNotFoundException;
import com.uni.ethesis.exceptions.TeacherNotFoundException;
import com.uni.ethesis.exceptions.ThesisNotFoundException;
import com.uni.ethesis.service.ReviewService;
import com.uni.ethesis.utils.mappers.ReviewMapper;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ThesisRepository thesisRepository;
    private final TeacherRepository teacherRepository;
    private final ReviewMapper reviewMapper;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             ThesisRepository thesisRepository,
                             TeacherRepository teacherRepository,
                             ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.thesisRepository = thesisRepository;
        this.teacherRepository = teacherRepository;
        this.reviewMapper = reviewMapper;
    }

    @Override
    @Transactional
    public ReviewDto createReview(ReviewDto reviewDto) {
        Review review = reviewMapper.reviewDtoToReview(reviewDto);
        
        // Set teacher
        Teacher teacher = teacherRepository.findById(reviewDto.getTeacherId())
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id: " + reviewDto.getTeacherId()));
        review.setTeacher(teacher);
        
        // Set thesis
        Thesis thesis = thesisRepository.findById(reviewDto.getThesisId())
                .orElseThrow(() -> new ThesisNotFoundException("Thesis not found with id: " + reviewDto.getThesisId()));
        review.setThesis(thesis);
        
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.reviewToReviewDto(savedReview);
    }

    @Override
    public ReviewDto getReviewById(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + id));
        return reviewMapper.reviewToReviewDto(review);
    }

    @Override
    public ReviewDto getReviewByThesisId(UUID thesisId) {
        Review review = reviewRepository.findByThesisId(thesisId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found for thesis id: " + thesisId));
        return reviewMapper.reviewToReviewDto(review);
    }

    @Override
    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(reviewMapper::reviewToReviewDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> getReviewsByTeacherId(UUID teacherId) {
        return reviewRepository.findByTeacherId(teacherId).stream()
                .map(reviewMapper::reviewToReviewDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewDto updateReview(UUID id, ReviewDto reviewDto) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + id));

        // Update allowed fields
        if (reviewDto.getContent() != null) {
            existingReview.setContent(reviewDto.getContent());
        }
        if (reviewDto.getConclusion() != null) {
            existingReview.setConclusion(reviewDto.getConclusion());
        }

        Review updatedReview = reviewRepository.save(existingReview);
        return reviewMapper.reviewToReviewDto(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(UUID id) {
        if (!reviewRepository.existsById(id)) {
            throw new ReviewNotFoundException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ReviewDto submitReview(UUID thesisId, UUID teacherId, String content, ReviewConclusion conclusion) {
        // Check if thesis exists
        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new ThesisNotFoundException("Thesis not found with id: " + thesisId));

        // Check if teacher exists
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id: " + teacherId));

        // Validate thesis status
        if (thesis.getStatus() != ThesisStatus.WAITING_FOR_REVIEW) {
            throw new IllegalStateException("Thesis is not in WAITING_FOR_REVIEW status");
        }

        // Create review
        Review review = Review.builder()
                .content(content)
                .conclusion(conclusion)
                .teacher(teacher)
                .thesis(thesis)
                .build();

        Review savedReview = reviewRepository.save(review);

        // Update thesis status based on review conclusion
        if (conclusion == ReviewConclusion.ACCEPTED) {
            thesis.setStatus(ThesisStatus.WAITING_FOR_DEFENSE);
        } else {
            // If rejected, student needs to resubmit, so keep status as WAITING_FOR_REVIEW
            // or you could create a new status like NEEDS_REVISION
            thesis.setStatus(ThesisStatus.WAITING_FOR_REVIEW);
        }
        thesisRepository.save(thesis);

        return reviewMapper.reviewToReviewDto(savedReview);
    }

    @Override
    public boolean hasThesisBeenReviewed(UUID thesisId) {
        return reviewRepository.existsByThesisId(thesisId);
    }

    @Override
    public ReviewDto getLatestReviewForThesis(UUID thesisId) {
        Review review = reviewRepository.findLatestByThesisId(thesisId)
                .orElseThrow(() -> new ReviewNotFoundException("No review found for thesis id: " + thesisId));
        return reviewMapper.reviewToReviewDto(review);
    }

    @Override
    public boolean canProceedToDefense(UUID thesisId) {
        Optional<Review> latestReview = reviewRepository.findLatestByThesisId(thesisId);
        if (latestReview.isEmpty()) {
            return false;
        }
        return latestReview.get().getConclusion() == ReviewConclusion.ACCEPTED;
    }

    @Override
    @Transactional
    public void markThesisReadyForDefense(UUID thesisId) {
        if (!canProceedToDefense(thesisId)) {
            throw new IllegalStateException("Thesis cannot proceed to defense - review not accepted");
        }

        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new ThesisNotFoundException("Thesis not found with id: " + thesisId));

        thesis.setStatus(ThesisStatus.WAITING_FOR_DEFENSE);
        thesisRepository.save(thesis);
    }

    @Override
    public long countReviewsByConclusion(ReviewConclusion conclusion) {
        return reviewRepository.countByConclusion(conclusion);
    }

    @Override
    public long countNegativeReviews() {
        return reviewRepository.countByConclusion(ReviewConclusion.REJECTED);
    }

    @Override
    public List<ReviewDto> getReviewsByConclusion(ReviewConclusion conclusion) {
        return reviewRepository.findByConclusion(conclusion).stream()
                .map(reviewMapper::reviewToReviewDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> getThesesAwaitingReview() {
        // Find all theses with WAITING_FOR_REVIEW status that don't have reviews yet
        return thesisRepository.findAll().stream()
                .filter(thesis -> thesis.getStatus() == ThesisStatus.WAITING_FOR_REVIEW)
                .filter(thesis -> !reviewRepository.existsByThesisId(thesis.getId()))
                .map(thesis -> ReviewDto.builder()
                        .thesisId(thesis.getId())
                        .build())
                .collect(Collectors.toList());
    }
}
