package com.uni.ethesis.web.view.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.enums.ThesisProposalStatus;
import com.uni.ethesis.service.ThesisApplicationService;
import com.uni.ethesis.utils.AuthenticationUtils;
import com.uni.ethesis.utils.mappers.ThesisApplicationMapper;
import com.uni.ethesis.web.view.model.ThesisProposalViewModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/proposals")
@RequiredArgsConstructor
public class ProposalController {

    @Autowired
    private ThesisApplicationService thesisApplicationService;
    
    @Autowired
    private ThesisApplicationMapper thesisApplicationMapper;

    @GetMapping("/browse")
    @PreAuthorize("hasRole('STUDENT')")
    public String browseProposals(Model model, @RequestParam(required = false) String search) {
        try {
            List<ThesisProposalDto> proposalDtos;
            
            if (search != null && !search.trim().isEmpty()) {
                // For now, get all and filter - in a real app, you'd implement search in the service
                proposalDtos = thesisApplicationService.getAllThesisProposals().stream()
                    .filter(p -> p.getStatus() == ThesisProposalStatus.PENDING)
                    .filter(p -> p.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                                p.getGoal().toLowerCase().contains(search.toLowerCase()) ||
                                p.getTechnology().toLowerCase().contains(search.toLowerCase()))
                    .toList();
                model.addAttribute("searchQuery", search);
            } else {
                // Get all pending proposals (available for application)
                proposalDtos = thesisApplicationService.getThesisProposalsByStatus(ThesisProposalStatus.PENDING);
            }
            
            List<ThesisProposalViewModel> proposals = proposalDtos.stream()
                    .map(thesisApplicationMapper::toViewModel)
                    .toList();
            model.addAttribute("proposals", proposals);
            model.addAttribute("title", "Browse Available Proposals");
            return "proposals/browse";
        } catch (Exception e) {
            log.error("Error browsing proposals", e);
            model.addAttribute("error", "Unable to load proposals. Please try again.");
            return "error";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public String proposalDetails(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ThesisProposalDto proposalDto = thesisApplicationService.getThesisProposalById(id);
            ThesisProposalViewModel proposal = thesisApplicationMapper.toViewModel(proposalDto);
            model.addAttribute("proposal", proposal);
            
            // Check if current user is a student and can apply
            if (AuthenticationUtils.isStudent()) {
                UUID studentId = AuthenticationUtils.getCurrentUserId();
                // Check if student already has proposals with this teacher
                List<ThesisProposalDto> studentProposals = thesisApplicationService.getThesisProposalsByStudentId(studentId);
                boolean hasAppliedToTeacher = studentProposals.stream()
                    .anyMatch(p -> p.getTeacherId().equals(proposalDto.getTeacherId()));
                
                model.addAttribute("canApply", !hasAppliedToTeacher && proposalDto.getStatus() == ThesisProposalStatus.PENDING);
                model.addAttribute("hasAppliedToTeacher", hasAppliedToTeacher);
            }
            
            model.addAttribute("title", "Proposal Details");
            return "proposals/details";
        } catch (Exception e) {
            log.error("Error loading proposal details", e);
            redirectAttributes.addFlashAttribute("error", "Unable to load proposal details. Please try again.");
            return "redirect:/proposals/browse";
        }
    }

    @PostMapping("/{id}/apply")
    @PreAuthorize("hasRole('STUDENT')")
    public String applyForProposal(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            UUID studentId = AuthenticationUtils.getCurrentUserId();
            ThesisProposalDto originalProposal = thesisApplicationService.getThesisProposalById(id);
            
            // Check if student can apply
            List<ThesisProposalDto> studentProposals = thesisApplicationService.getThesisProposalsByStudentId(studentId);
            boolean hasAppliedToTeacher = studentProposals.stream()
                .anyMatch(p -> p.getTeacherId().equals(originalProposal.getTeacherId()));
            
            if (hasAppliedToTeacher) {
                redirectAttributes.addFlashAttribute("error", "You have already applied to this teacher.");
                return "redirect:/proposals/browse";
            }
            
            if (originalProposal.getStatus() != ThesisProposalStatus.PENDING) {
                redirectAttributes.addFlashAttribute("error", "This proposal is no longer available for application.");
                return "redirect:/proposals/browse";
            }
            
            // Create a new proposal application based on the original
            ThesisProposalDto applicationDto = ThesisProposalDto.builder()
                .title(originalProposal.getTitle())
                .goal(originalProposal.getGoal())
                .objectives(originalProposal.getObjectives())
                .technology(originalProposal.getTechnology())
                .studentId(studentId)
                .teacherId(originalProposal.getTeacherId())
                .status(ThesisProposalStatus.PENDING)
                .build();
            
            ThesisProposalDto createdApplication = thesisApplicationService.createThesisProposal(applicationDto);
            
            redirectAttributes.addFlashAttribute("message", 
                "Successfully applied for proposal: " + createdApplication.getTitle());
            return "redirect:/student/proposals";
        } catch (Exception e) {
            log.error("Error applying for proposal", e);
            redirectAttributes.addFlashAttribute("error", "Unable to apply for proposal. Please try again.");
            return "redirect:/proposals/" + id;
        }
    }
}
