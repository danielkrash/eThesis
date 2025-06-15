package com.uni.ethesis.web.view.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.uni.ethesis.data.dto.ThesisDto;
import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.service.ThesisApplicationService;
import com.uni.ethesis.service.ThesisService;
import com.uni.ethesis.utils.AuthenticationUtils;
import com.uni.ethesis.utils.mappers.ThesisApplicationMapper;
import com.uni.ethesis.web.view.model.ThesisProposalViewModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    @Autowired
    private ThesisApplicationService thesisApplicationService;
    
    @Autowired
    private ThesisService thesisService;
    
    @Autowired
    private ThesisApplicationMapper thesisApplicationMapper;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            UUID studentId = AuthenticationUtils.getCurrentUserId();
            
            // Get student's proposals
            List<ThesisProposalDto> proposalDtos = thesisApplicationService.getThesisProposalsByStudentId(studentId);
            List<ThesisProposalViewModel> proposals = proposalDtos.stream()
                    .map(thesisApplicationMapper::toViewModel)
                    .toList();
            model.addAttribute("proposals", proposals);
            
            // Get student's thesis if exists
            try {
                ThesisDto thesis = thesisService.getThesisByStudentId(studentId);
                model.addAttribute("thesis", thesis);
                model.addAttribute("hasThesis", true);
            } catch (Exception e) {
                model.addAttribute("hasThesis", false);
            }
            
            // Count statistics
            long pendingProposals = proposalDtos.stream()
                .filter(p -> p.getStatus().toString().equals("PENDING"))
                .count();
            long approvedProposals = proposalDtos.stream()
                .filter(p -> p.getStatus().toString().equals("APPROVED"))
                .count();
            long rejectedProposals = proposalDtos.stream()
                .filter(p -> p.getStatus().toString().equals("REJECTED"))
                .count();
            
            model.addAttribute("pendingCount", pendingProposals);
            model.addAttribute("approvedCount", approvedProposals);
            model.addAttribute("rejectedCount", rejectedProposals);
            model.addAttribute("totalProposals", proposalDtos.size());
            
            model.addAttribute("title", "Student Dashboard");
            return "student/dashboard";
        } catch (Exception e) {
            log.error("Error loading student dashboard", e);
            model.addAttribute("error", "Unable to load dashboard. Please try again.");
            return "error";
        }
    }

    @GetMapping("/proposals")
    public String proposals(Model model) {
        try {
            UUID studentId = AuthenticationUtils.getCurrentUserId();
            List<ThesisProposalDto> proposalDtos = thesisApplicationService.getThesisProposalsByStudentId(studentId);
            List<ThesisProposalViewModel> proposals = proposalDtos.stream()
                    .map(thesisApplicationMapper::toViewModel)
                    .toList();
            
            model.addAttribute("proposals", proposals);
            model.addAttribute("title", "My Proposals");
            return "student/proposals";
        } catch (Exception e) {
            log.error("Error loading student proposals", e);
            model.addAttribute("error", "Unable to load proposals. Please try again.");
            return "error";
        }
    }

    @GetMapping("/thesis")
    public String thesis(Model model) {
        try {
            UUID studentId = AuthenticationUtils.getCurrentUserId();
            
            try {
                ThesisDto thesis = thesisService.getThesisByStudentId(studentId);
                model.addAttribute("thesis", thesis);
                model.addAttribute("hasThesis", true);
                
                // Check if student can proceed to defense
                boolean canProceedToDefense = thesisService.canStudentProceedToDefense(thesis.getId());
                model.addAttribute("canProceedToDefense", canProceedToDefense);
                
            } catch (Exception e) {
                model.addAttribute("hasThesis", false);
                model.addAttribute("message", "You don't have a thesis yet. Please wait for your proposal to be approved first.");
            }
            
            model.addAttribute("title", "My Thesis");
            return "student/thesis";
        } catch (Exception e) {
            log.error("Error loading student thesis", e);
            model.addAttribute("error", "Unable to load thesis information. Please try again.");
            return "error";
        }
    }

    @GetMapping("/proposal/{id}")
    public String proposalDetails(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            UUID studentId = AuthenticationUtils.getCurrentUserId();
            ThesisProposalDto proposalDto = thesisApplicationService.getThesisProposalById(id);
            
            // Check if this proposal belongs to the current student
            if (!proposalDto.getStudentId().equals(studentId)) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to view this proposal.");
                return "redirect:/student/proposals";
            }
            
            ThesisProposalViewModel proposal = thesisApplicationMapper.toViewModel(proposalDto);
            model.addAttribute("proposal", proposal);
            model.addAttribute("title", "Proposal Details");
            return "student/proposal-details";
        } catch (Exception e) {
            log.error("Error loading proposal details", e);
            redirectAttributes.addFlashAttribute("error", "Unable to load proposal details. Please try again.");
            return "redirect:/student/proposals";
        }
    }
}
