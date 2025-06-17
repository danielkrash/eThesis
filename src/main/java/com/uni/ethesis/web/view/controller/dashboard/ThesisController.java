package com.uni.ethesis.web.view.controller.dashboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.uni.ethesis.data.dto.CommentDto;
import com.uni.ethesis.data.dto.ReviewDto;
import com.uni.ethesis.data.dto.ThesisDto;
import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.data.dto.UserDto;
import com.uni.ethesis.enums.ReviewConclusion;
import com.uni.ethesis.enums.ThesisStatus;
import com.uni.ethesis.service.CommentService;
import com.uni.ethesis.service.FileStorageService;
import com.uni.ethesis.service.ReviewService;
import com.uni.ethesis.service.ThesisProposalService;
import com.uni.ethesis.service.ThesisService;
import com.uni.ethesis.service.UserService;
import com.uni.ethesis.service.UserViewService;
import com.uni.ethesis.utils.AuthenticationUtils;
import com.uni.ethesis.utils.mappers.CommentMapper;
import com.uni.ethesis.utils.mappers.ReviewMapper;
import com.uni.ethesis.utils.mappers.ThesisMapper;
import com.uni.ethesis.web.view.model.CommentViewModel;
import com.uni.ethesis.web.view.model.ThesisReviewViewModel;
import com.uni.ethesis.web.view.model.ThesisViewModel;
import com.uni.ethesis.web.view.model.UserViewModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/dashboard/thesis")
@RequiredArgsConstructor
public class ThesisController {

    private final ThesisService thesisService;
    private final ThesisProposalService thesisProposalService;
    private final UserViewService userViewService;
    private final ThesisMapper thesisMapper;
    private final ReviewMapper reviewMapper;
    private final CommentMapper commentMapper;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final ReviewService reviewService;
    private final CommentService commentService;

    @GetMapping("/{thesisId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public String viewThesis(@PathVariable UUID thesisId, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to access thesis view");
            return "redirect:/login";
        }

        try {
            // Get user information
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", user);

            // Get the thesis
            ThesisDto thesisDto;
            try {
                thesisDto = thesisService.getThesisById(thesisId);
            } catch (Exception e) {
                log.warn("Thesis with id {} not found", thesisId);
                model.addAttribute("error", "Thesis not found");
                return "dashboard/main";
            }

            // Get the related proposal
            ThesisProposalDto proposalDto = thesisProposalService.getThesisProposalById(thesisDto.getProposalId());
            
            // Check permissions
            UUID currentUserId = AuthenticationUtils.getCurrentUserId();
            boolean canView = false;
            
            if (user.getRole().contains("admin")) {
                canView = true;
            } else if (user.getRole().contains("teacher") && proposalDto.getTeacherId().equals(currentUserId)) {
                canView = true;
            } else if (user.getRole().contains("student") && proposalDto.getStudentId().equals(currentUserId)) {
                canView = true;
            }

            if (!canView) {
                log.warn("User {} does not have permission to view thesis {}", user.getEmail(), thesisId);
                model.addAttribute("error", "You don't have permission to view this thesis");
                return "dashboard/main";
            }

            // Convert to ViewModel
            ThesisViewModel thesis = thesisMapper.thesisDtoToViewModel(thesisDto);
            
            // Populate additional information from proposal
            thesis.setTitle(proposalDto.getTitle());
            thesis.setGoal(proposalDto.getGoal());
            thesis.setObjectives(proposalDto.getObjectives());
            thesis.setTechnology(proposalDto.getTechnology());
            thesis.setStudentId(proposalDto.getStudentId().toString());
            thesis.setTeacherId(proposalDto.getTeacherId().toString());
            thesis.setDepartmentId(proposalDto.getDepartmentId().toString());
            
            // Get and set user names
            UserDto student = userService.getUserById(proposalDto.getStudentId());
            UserDto teacher = userService.getUserById(proposalDto.getTeacherId());
            thesis.setStudentName(student.getFirstName() + " " + student.getLastName());
            thesis.setTeacherName(teacher.getFirstName() + " " + teacher.getLastName());
            
            // Get and populate reviews with comments
            List<ThesisReviewViewModel> reviewViewModels = new ArrayList<>();
            try {
                List<ReviewDto> reviews = reviewService.getReviewsByThesisId(thesisId);
                for (ReviewDto reviewDto : reviews) {
                    ThesisReviewViewModel reviewViewModel = reviewMapper.reviewDtoToThesisReviewViewModel(reviewDto);
                    
                    // Set reviewer name
                    try {
                        var reviewer = userService.getUserById(reviewDto.getTeacherId());
                        reviewViewModel.setReviewerName(reviewer.getFirstName() + " " + reviewer.getLastName());
                    } catch (Exception e) {
                        log.warn("Could not load reviewer name for review {}: {}", reviewDto.getId(), e.getMessage());
                        reviewViewModel.setReviewerName("Unknown Reviewer");
                    }
                    
                    // Get comments for this review
                    List<CommentDto> comments = commentService.getCommentsByReviewId(reviewDto.getId());
                    List<CommentViewModel> commentViewModels = new ArrayList<>();
                    
                    for (CommentDto commentDto : comments) {
                        CommentViewModel commentViewModel = commentMapper.commentDtoToCommentViewModel(commentDto);
                        
                        // Set commenter name
                        try {
                            var commenter = userService.getUserById(commentDto.getUserId());
                            commentViewModel.setCommenterName(commenter.getFirstName() + " " + commenter.getLastName());
                        } catch (Exception e) {
                            log.warn("Could not get commenter name for comment {}: {}", commentDto.getId(), e.getMessage());
                            commentViewModel.setCommenterName("Unknown");
                        }
                        
                        commentViewModels.add(commentViewModel);
                    }
                    
                    reviewViewModel.setComments(commentViewModels);
                    
                    reviewViewModels.add(reviewViewModel);
                }
            } catch (Exception e) {
                log.warn("Could not load reviews for thesis {}: {}", thesisId, e.getMessage());
                // Continue without reviews if there's an error
            }
            thesis.setReviews(reviewViewModels);
            
            model.addAttribute("thesis", thesis);
            model.addAttribute("proposal", proposalDto);

            // Determine user's role in relation to this thesis
            boolean isOwner = user.getRole().contains("student") && proposalDto.getStudentId().equals(currentUserId);
            boolean isSupervisor = user.getRole().contains("teacher") && proposalDto.getTeacherId().equals(currentUserId);
            boolean isAdmin = user.getRole().contains("admin");
            
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("isSupervisor", isSupervisor);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("canUpload", isOwner);
            model.addAttribute("canReview", isSupervisor || isAdmin);

            log.info("Thesis {} viewed by user: {}", thesisId, user.getEmail());
            return "dashboard/thesis/view";

        } catch (Exception e) {
            log.error("Error loading thesis view", e);
            model.addAttribute("error", "Failed to load thesis details");
            return "dashboard/main";
        }
    }

    @GetMapping("/{thesisId}/upload")
    @PreAuthorize("hasRole('STUDENT')")
    public String showUploadForm(@PathVariable UUID thesisId, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to access thesis upload");
            return "redirect:/login";
        }

        try {
            // Get user information
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", user);

            // Get the thesis
            ThesisDto thesisDto = thesisService.getThesisById(thesisId);
            ThesisProposalDto proposalDto = thesisProposalService.getThesisProposalById(thesisDto.getProposalId());
            
            // Check if user owns this thesis
            UUID currentUserId = AuthenticationUtils.getCurrentUserId();
            if (!proposalDto.getStudentId().equals(currentUserId)) {
                log.warn("User {} does not own thesis {}", user.getEmail(), thesisId);
                model.addAttribute("error", "You can only upload files for your own thesis");
                return "redirect:/dashboard/thesis/" + thesisId;
            }

            ThesisViewModel thesis = thesisMapper.thesisDtoToViewModel(thesisDto);
            thesis.setGoal(proposalDto.getGoal());
            thesis.setObjectives(proposalDto.getObjectives());
            thesis.setTechnology(proposalDto.getTechnology());
            thesis.setTitle(proposalDto.getTitle());
            var student = userService.getUserById(proposalDto.getStudentId());
            var teacher = userService.getUserById(proposalDto.getTeacherId());
            thesis.setStudentName(student.getFirstName() + " " + student.getLastName());
            thesis.setTeacherName(teacher.getFirstName() + " " + teacher.getLastName());
            model.addAttribute("thesis", thesis);

            return "dashboard/thesis/upload";

        } catch (Exception e) {
            log.error("Error loading thesis upload form", e);
            model.addAttribute("error", "Failed to load upload form");
            return "redirect:/dashboard/thesis/" + thesisId;
        }
    }

    @PostMapping("/{thesisId}/upload")
    @PreAuthorize("hasRole('STUDENT')")
    public String uploadThesisPdf(@PathVariable UUID thesisId,
                                 @RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes,
                                 Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to upload thesis");
            return "redirect:/login";
        }

        try {
            // Validate file
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
                return "redirect:/dashboard/thesis/" + thesisId + "/upload";
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                redirectAttributes.addFlashAttribute("error", "Only PDF files are allowed");
                return "redirect:/dashboard/thesis/" + thesisId + "/upload";
            }

            // Get the thesis and check ownership
            ThesisDto thesisDto = thesisService.getThesisById(thesisId);
            ThesisProposalDto proposalDto = thesisProposalService.getThesisProposalById(thesisDto.getProposalId());
            
            UUID currentUserId = AuthenticationUtils.getCurrentUserId();
            if (!proposalDto.getStudentId().equals(currentUserId)) {
                redirectAttributes.addFlashAttribute("error", "You can only upload files for your own thesis");
                return "redirect:/dashboard/thesis/" + thesisId;
            }

            // Store the file
            String fileName = fileStorageService.storeFile(file, "thesis");
            
            // Update thesis with PDF path and reset status to WAITING_FOR_REVIEW
            thesisDto.setPdfPath(fileName);
            thesisDto.setStatus(ThesisStatus.WAITING_FOR_REVIEW);
            thesisService.updateThesis(thesisId, thesisDto);

            // If thesis has existing reviews, mark the latest one as rejected
            try {
                if (reviewService.hasThesisBeenReviewed(thesisId)) {
                    var latestReview = reviewService.getLatestReviewForThesis(thesisId);
                    if (latestReview != null && latestReview.getConclusion() != ReviewConclusion.REJECTED) {
                        latestReview.setConclusion(ReviewConclusion.REJECTED);
                        reviewService.updateReview(latestReview.getId(), latestReview);
                        log.info("Marked latest review as rejected for thesis {} due to new file upload", thesisId);
                    }
                }
            } catch (Exception e) {
                log.warn("Could not update review status for thesis {}: {}", thesisId, e.getMessage());
                // Continue with upload even if review update fails
            }

            log.info("Thesis PDF uploaded successfully for thesis {} by user: {}", thesisId, currentUserId);
            redirectAttributes.addFlashAttribute("success", "Thesis PDF uploaded successfully! Status reset to waiting for review.");
            return "redirect:/dashboard/thesis/" + thesisId;

        } catch (IOException e) {
            log.error("Error uploading thesis PDF", e);
            redirectAttributes.addFlashAttribute("error", "Failed to upload file. Please try again.");
            return "redirect:/dashboard/thesis/" + thesisId + "/upload";
        } catch (Exception e) {
            log.error("Error processing thesis upload", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while uploading. Please try again.");
            return "redirect:/dashboard/thesis/" + thesisId + "/upload";
        }
    }

    @GetMapping("/{thesisId}/review")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public String showReviewForm(@PathVariable UUID thesisId, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to access thesis review");
            return "redirect:/login";
        }

        try {
            // Get user information
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", user);

            // Get the thesis
            ThesisDto thesisDto = thesisService.getThesisById(thesisId);
            ThesisProposalDto proposalDto = thesisProposalService.getThesisProposalById(thesisDto.getProposalId());
            
            // Check permissions
            UUID currentUserId = AuthenticationUtils.getCurrentUserId();
            boolean canReview = false;
            
            if (user.getRole().contains("admin")) {
                canReview = true;
            } else if (user.getRole().contains("teacher") && proposalDto.getTeacherId().equals(currentUserId)) {
                canReview = true;
            }

            if (!canReview) {
                log.warn("User {} does not have permission to review thesis {}", user.getEmail(), thesisId);
                model.addAttribute("error", "You don't have permission to review this thesis");
                return "redirect:/dashboard/thesis/" + thesisId;
            }

            // Convert to ViewModel and populate additional information
            ThesisViewModel thesis = thesisMapper.thesisDtoToViewModel(thesisDto);
            
            // Populate additional information from proposal
            thesis.setTitle(proposalDto.getTitle());
            thesis.setGoal(proposalDto.getGoal());
            thesis.setObjectives(proposalDto.getObjectives());
            thesis.setTechnology(proposalDto.getTechnology());
            thesis.setStudentId(proposalDto.getStudentId().toString());
            thesis.setTeacherId(proposalDto.getTeacherId().toString());
            thesis.setDepartmentId(proposalDto.getDepartmentId().toString());
            
            // Get student and teacher names
            var student = userService.getUserById(proposalDto.getStudentId());
            var teacher = userService.getUserById(proposalDto.getTeacherId());
            thesis.setStudentName(student.getFirstName() + " " + student.getLastName());
            thesis.setTeacherName(teacher.getFirstName() + " " + teacher.getLastName());
            
            model.addAttribute("thesis", thesis);

            return "dashboard/thesis/review";

        } catch (Exception e) {
            log.error("Error loading thesis review form", e);
            model.addAttribute("error", "Failed to load review form");
            return "redirect:/dashboard/thesis/" + thesisId;
        }
    }

    @PostMapping("/{thesisId}/review")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public String submitReview(@PathVariable UUID thesisId,
                              @RequestParam String content,
                              @RequestParam String conclusion,
                              RedirectAttributes redirectAttributes,
                              Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to submit review");
            return "redirect:/login";
        }

        try {
            // Get user information
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            UUID currentUserId = AuthenticationUtils.getCurrentUserId();

            // Get the thesis and check permissions
            ThesisDto thesisDto = thesisService.getThesisById(thesisId);
            ThesisProposalDto proposalDto = thesisProposalService.getThesisProposalById(thesisDto.getProposalId());
            
            // Check if user has permission to review (supervisor teacher or admin)
            boolean canReview = false;
            
            if (user.getRole().contains("admin")) {
                canReview = true;
            } else if (user.getRole().contains("teacher") && proposalDto.getTeacherId().equals(currentUserId)) {
                canReview = true;
            }

            if (!canReview) {
                log.warn("User {} does not have permission to review thesis {}", user.getEmail(), thesisId);
                redirectAttributes.addFlashAttribute("error", "You don't have permission to review this thesis");
                return "redirect:/dashboard/thesis/" + thesisId;
            }

            // Validate input
            if (content == null || content.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Review content cannot be empty");
                return "redirect:/dashboard/thesis/" + thesisId + "/review";
            }

            if (conclusion == null || (!conclusion.equals("ACCEPTED") && !conclusion.equals("REJECTED"))) {
                redirectAttributes.addFlashAttribute("error", "Please select a valid conclusion (ACCEPTED or REJECTED)");
                return "redirect:/dashboard/thesis/" + thesisId + "/review";
            }

            // Convert string to enum
            ReviewConclusion reviewConclusion = ReviewConclusion.valueOf(conclusion);
            
            // Submit the review using the ReviewService
            reviewService.submitReview(thesisId, currentUserId, content.trim(), reviewConclusion);
            
            // Update thesis status based on review conclusion
            if (reviewConclusion == ReviewConclusion.ACCEPTED) {
                thesisDto.setStatus(ThesisStatus.READY_FOR_DEFENSE);
            } else {
                thesisDto.setStatus(ThesisStatus.WAITING_FOR_REVIEW);
            }
            thesisService.updateThesis(thesisId, thesisDto);
            
            log.info("Review submitted for thesis {} by supervisor: {} with conclusion: {}", 
                    thesisId, user.getEmail(), conclusion);
            redirectAttributes.addFlashAttribute("success", "Review submitted successfully!");
            return "redirect:/dashboard/thesis/" + thesisId;

        } catch (Exception e) {
            log.error("Error submitting review", e);
            redirectAttributes.addFlashAttribute("error", "Failed to submit review. Please try again.");
            return "redirect:/dashboard/thesis/" + thesisId + "/review";
        }
    }

    @PostMapping("/{thesisId}/review/{reviewId}/comment")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public String addComment(@PathVariable UUID thesisId, 
                           @PathVariable UUID reviewId,
                           @RequestParam String content,
                           RedirectAttributes redirectAttributes,
                           Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to add comment");
            return "redirect:/login";
        }

        try {
            // Get user information
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            UUID currentUserId = AuthenticationUtils.getCurrentUserId();

            // Get the thesis and check permissions
            ThesisDto thesisDto = thesisService.getThesisById(thesisId);
            ThesisProposalDto proposalDto = thesisProposalService.getThesisProposalById(thesisDto.getProposalId());
            
            // Check if user has permission to comment (student owner, supervisor teacher, or admin)
            boolean canComment = false;
            
            if (user.getRole().contains("admin")) {
                canComment = true;
            } else if (user.getRole().contains("teacher") && proposalDto.getTeacherId().equals(currentUserId)) {
                canComment = true;
            } else if (user.getRole().contains("student") && proposalDto.getStudentId().equals(currentUserId)) {
                canComment = true;
            }

            if (!canComment) {
                log.warn("User {} does not have permission to comment on thesis {}", user.getEmail(), thesisId);
                redirectAttributes.addFlashAttribute("error", "You don't have permission to comment on this thesis");
                return "redirect:/dashboard/thesis/" + thesisId;
            }

            // Validate content
            if (content == null || content.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Comment content cannot be empty");
                return "redirect:/dashboard/thesis/" + thesisId;
            }

            // Create the comment
            commentService.createComment(reviewId, currentUserId, content.trim());
            
            log.info("Comment added to review {} by user: {}", reviewId, user.getEmail());
            redirectAttributes.addFlashAttribute("success", "Comment added successfully!");
            return "redirect:/dashboard/thesis/" + thesisId;

        } catch (Exception e) {
            log.error("Error adding comment", e);
            redirectAttributes.addFlashAttribute("error", "Failed to add comment. Please try again.");
            return "redirect:/dashboard/thesis/" + thesisId;
        }
    }

    @PostMapping("/{thesisId}/start-defense")
    @PreAuthorize("hasRole('STUDENT')")
    public String startDefense(@PathVariable UUID thesisId,
                              RedirectAttributes redirectAttributes,
                              Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to start defense");
            return "redirect:/login";
        }

        try {
            // Get user information
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            UUID currentUserId = AuthenticationUtils.getCurrentUserId();

            // Get the thesis and check permissions
            ThesisDto thesisDto = thesisService.getThesisById(thesisId);
            ThesisProposalDto proposalDto = thesisProposalService.getThesisProposalById(thesisDto.getProposalId());
            
            // Check if user owns this thesis
            if (!proposalDto.getStudentId().equals(currentUserId)) {
                log.warn("User {} does not own thesis {}", user.getEmail(), thesisId);
                redirectAttributes.addFlashAttribute("error", "You can only start defense for your own thesis");
                return "redirect:/dashboard/thesis/" + thesisId;
            }

            // Check if thesis is in the correct status
            if (thesisDto.getStatus() != ThesisStatus.READY_FOR_DEFENSE) {
                log.warn("Thesis {} is not ready for defense. Current status: {}", thesisId, thesisDto.getStatus());
                redirectAttributes.addFlashAttribute("error", "Thesis is not ready for defense. Current status: " + thesisDto.getStatus());
                return "redirect:/dashboard/thesis/" + thesisId;
            }

            // Update thesis status to WAITING_FOR_DEFENSE
            thesisDto.setStatus(ThesisStatus.WAITING_FOR_DEFENSE);
            thesisService.updateThesis(thesisId, thesisDto);
            
            log.info("Defense started for thesis {} by student: {}", thesisId, user.getEmail());
            redirectAttributes.addFlashAttribute("success", "Defense process started successfully! Your thesis is now waiting for defense scheduling.");
            return "redirect:/dashboard/thesis/" + thesisId;

        } catch (Exception e) {
            log.error("Error starting defense for thesis {}", thesisId, e);
            redirectAttributes.addFlashAttribute("error", "Failed to start defense process. Please try again.");
            return "redirect:/dashboard/thesis/" + thesisId;
        }
    }
}
