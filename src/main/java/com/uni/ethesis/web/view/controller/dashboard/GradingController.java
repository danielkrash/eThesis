package com.uni.ethesis.web.view.controller.dashboard;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.uni.ethesis.data.dto.DefenseSessionDto;
import com.uni.ethesis.data.dto.DefenseSessionProfessorDto;
import com.uni.ethesis.data.dto.ThesisDto;
import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.data.dto.UserDto;
import com.uni.ethesis.enums.ThesisStatus;
import com.uni.ethesis.service.DefenseSessionProfessorService;
import com.uni.ethesis.service.DefenseSessionService;
import com.uni.ethesis.service.ThesisProposalService;
import com.uni.ethesis.service.ThesisService;
import com.uni.ethesis.service.UserService;
import com.uni.ethesis.utils.GradingUtil;
import com.uni.ethesis.web.view.model.GradingSessionViewModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/dashboard/grading")
@PreAuthorize("hasRole('TEACHER')")
public class GradingController {

    private final DefenseSessionService defenseSessionService;
    private final DefenseSessionProfessorService defenseSessionProfessorService;
    private final ThesisService thesisService;
    private final UserService userService;
    private final ThesisProposalService thesisProposalService;
    
    @Autowired
    public GradingController(
            DefenseSessionService defenseSessionService,
            DefenseSessionProfessorService defenseSessionProfessorService,
            ThesisService thesisService,
            UserService userService,
            ThesisProposalService thesisProposalService) {
        this.defenseSessionService = defenseSessionService;
        this.defenseSessionProfessorService = defenseSessionProfessorService;
        this.thesisService = thesisService;
        this.userService = userService;
        this.thesisProposalService = thesisProposalService;
    }
    
    @GetMapping
    public String gradingPage(Model model, Authentication authentication) {
        UUID professorId = UUID.fromString(authentication.getName());
        
        // Get defense sessions for this professor
        List<DefenseSessionProfessorDto> professorSessions = 
                defenseSessionProfessorService.getDefenseSessionsByProfessor(professorId);
        
        // Map to view models with detailed information
        List<GradingSessionViewModel> gradingSessions = mapToGradingSessionViewModels(professorSessions);
        
        model.addAttribute("sessions", gradingSessions);
        return "dashboard/grading/list";
    }
    
    @GetMapping("/{sessionId}")
    public String gradingFormPage(@PathVariable UUID sessionId, Model model, Authentication authentication) {
        UUID professorId = UUID.fromString(authentication.getName());
        
        try {
            // Get defense session
            DefenseSessionDto sessionDto = defenseSessionService.getDefenseSessionById(sessionId);
            if (sessionDto == null) {
                model.addAttribute("error", "Defense session not found.");
                return "dashboard/grading/form";
            }
            
            // Get thesis details
            ThesisDto thesisDto = thesisService.getThesisById(sessionDto.getThesisId());
            if (thesisDto == null) {
                model.addAttribute("error", "Thesis not found for this defense session.");
                return "dashboard/grading/form";
            }
            
            // Check if professor is assigned to this defense
            if (!defenseSessionProfessorService.isProfessorAssignedToDefenseSession(sessionId, professorId)) {
                model.addAttribute("error", "You are not assigned to this defense session.");
                return "dashboard/grading/form";
            }
            
            // Get professor's current evaluation if any
            DefenseSessionProfessorDto professorEvaluation = 
                    defenseSessionProfessorService.getProfessorEvaluation(sessionId, professorId);
                    
            // Get other committee members and their status
            List<DefenseSessionProfessorDto> committeeMembers = 
                    defenseSessionProfessorService.getProfessorsByDefenseSession(sessionId);
                    
            long totalMembers = committeeMembers.size();
            long gradedMembers = committeeMembers.stream().filter(member -> member.getGrade() != null).count();
                    
            // Map to session view model with detailed information
            GradingSessionViewModel sessionViewModel = mapToGradingSessionViewModel(sessionDto, professorEvaluation, thesisDto);
            
            model.addAttribute("gradingSession", sessionViewModel);
            model.addAttribute("professorEvaluation", professorEvaluation);
            model.addAttribute("thesis", thesisDto);
            model.addAttribute("totalCommitteeMembers", totalMembers);
            model.addAttribute("gradedMembersCount", gradedMembers);
            model.addAttribute("allGraded", gradedMembers == totalMembers);
            model.addAttribute("currentGrade", professorEvaluation != null ? professorEvaluation.getGrade() : null);
            model.addAttribute("currentThoughts", professorEvaluation != null ? professorEvaluation.getThoughts() : null);
            
            return "dashboard/grading/form";
            
        } catch (Exception e) {
            log.error("Error loading grading form for session {}: {}", sessionId, e.getMessage(), e);
            model.addAttribute("error", "Failed to load grading form. Please try again later.");
            return "dashboard/grading/form";
        }
    }
    
    @PostMapping("/{sessionId}")
    public String submitGrade(
            @PathVariable UUID sessionId,
            @RequestParam Integer grade,
            @RequestParam String thoughts,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        UUID professorId = UUID.fromString(authentication.getName());
        
        try {
            // Validate grade
            if (grade < 0 || grade > 100) {
                redirectAttributes.addFlashAttribute("error", "Grade must be between 0 and 100.");
                return "redirect:/dashboard/grading/" + sessionId;
            }
            
            // Update the defense session professor record
            defenseSessionProfessorService.updateProfessorEvaluation(sessionId, professorId, grade, thoughts);
                    
            // Check if all committee members have submitted grades
            if (defenseSessionProfessorService.areAllProfessorsGraded(sessionId)) {
                // Calculate average score
                Double averageGrade = defenseSessionProfessorService.getAverageGradeForDefenseSession(sessionId);
                
                if (averageGrade != null) {
                    // Get the defense session
                    DefenseSessionDto sessionDto = defenseSessionService.getDefenseSessionById(sessionId);
                    
                    // Get the thesis
                    ThesisDto thesisDto = thesisService.getThesisById(sessionDto.getThesisId());
                    
                    // Convert average score to appropriate grade using GradingUtil
                    BigDecimal finalGrade = GradingUtil.calculateGrade(averageGrade.intValue());
                    
                    // Update thesis with final grade and change status to DEFENDED
                    thesisDto.setFinalGrade(finalGrade);
                    thesisDto.setStatus(ThesisStatus.DEFENDED);
                    thesisService.updateThesis(thesisDto.getId(), thesisDto);
                    
                    redirectAttributes.addFlashAttribute("success", 
                        "Your grade has been submitted! All committee members have now graded. The thesis has received a final grade of " + finalGrade);
                } else {
                    redirectAttributes.addFlashAttribute("error", 
                        "Your grade has been submitted, but there was an issue calculating the final average grade.");
                }
            } else {
                redirectAttributes.addFlashAttribute("success", 
                    "Your grade has been submitted successfully. Waiting for other committee members to submit their grades.");
            }
            
            return "redirect:/dashboard/grading";
            
        } catch (Exception e) {
            log.error("Error submitting grade for session {}: {}", sessionId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to submit grade. Please try again later.");
            return "redirect:/dashboard/grading/" + sessionId;
        }
    }
    
    private List<GradingSessionViewModel> mapToGradingSessionViewModels(List<DefenseSessionProfessorDto> professorSessions) {
        List<GradingSessionViewModel> result = new ArrayList<>();
        
        for (DefenseSessionProfessorDto professorSession : professorSessions) {
            try {
                UUID sessionId = professorSession.getDefenseSessionId();
                DefenseSessionDto sessionDto = defenseSessionService.getDefenseSessionById(sessionId);
                
                if (sessionDto != null && sessionDto.getThesisId() != null) {
                    ThesisDto thesisDto = thesisService.getThesisById(sessionDto.getThesisId());
                    
                    if (thesisDto != null) {
                        GradingSessionViewModel viewModel = mapToGradingSessionViewModel(sessionDto, professorSession, thesisDto);
                        result.add(viewModel);
                    }
                }
            } catch (Exception e) {
                log.error("Error mapping defense session {} to grading view model: {}", 
                    professorSession.getDefenseSessionId(), e.getMessage());
            }
        }
        
        return result;
    }
    
    private GradingSessionViewModel mapToGradingSessionViewModel(
            DefenseSessionDto sessionDto, 
            DefenseSessionProfessorDto professorSession,
            ThesisDto thesisDto) {
        
        // Get student name and proposal title from the proposal
        String studentFullName = "Unknown Student";
        String proposalTitle = "Unknown Title";
        
        try {
            // Get proposal information
            UUID proposalId = thesisDto.getProposalId();
            if (proposalId != null) {
                // Get thesis proposal using ThesisProposalService
                ThesisProposalDto proposalDto = thesisProposalService.getThesisProposalById(proposalId);
                if (proposalDto != null) {
                    // Set proposal title from the actual proposal
                    proposalTitle = proposalDto.getTitle();
                    
                    // Get student information from the proposal
                    UUID studentId = proposalDto.getStudentId();
                    if (studentId != null) {
                        UserDto studentDto = userService.getUserById(studentId);
                        if (studentDto != null) {
                            studentFullName = studentDto.getFirstName() + " " + studentDto.getLastName();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not retrieve information for thesis {}: {}", thesisDto.getId(), e.getMessage());
        }
        
        // Get grading status
        boolean isGraded = professorSession != null && professorSession.getGrade() != null;
        Integer currentGrade = isGraded && professorSession != null ? professorSession.getGrade() : null;
        
        return GradingSessionViewModel.builder()
                .id(sessionDto.getId())
                .dateAndTime(sessionDto.getDateAndTime())
                .thesisId(thesisDto.getId())
                .studentName(studentFullName)
                .proposalTitle(proposalTitle)
                .graded(isGraded)
                .grade(currentGrade)
                .thoughts(professorSession != null ? professorSession.getThoughts() : null)
                .build();
    }
}
