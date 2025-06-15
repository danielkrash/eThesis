package com.uni.ethesis.web.view.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.uni.ethesis.data.dto.ThesisDto;
import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.enums.ThesisProposalStatus;
import com.uni.ethesis.service.ThesisApplicationService;
import com.uni.ethesis.service.ThesisService;
import com.uni.ethesis.utils.AuthenticationUtils;
import com.uni.ethesis.utils.mappers.ThesisApplicationMapper;
import com.uni.ethesis.web.view.model.ThesisProposalViewModel;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {

    @Autowired
    private ThesisApplicationService thesisApplicationService;
    
    @Autowired
    private ThesisService thesisService;
    
    @Autowired
    private ThesisApplicationMapper thesisApplicationMapper;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            UUID teacherId = AuthenticationUtils.getCurrentUserId();
            
            // Get teacher's proposals
            List<ThesisProposalDto> proposalDtos = thesisApplicationService.getThesisProposalsByTeacherId(teacherId);
            List<ThesisProposalViewModel> proposals = proposalDtos.stream()
                .map(thesisApplicationMapper::toViewModel)
                .toList();
            model.addAttribute("proposals", proposals);
            
            // Get supervised theses
            List<ThesisDto> theses = thesisService.findThesesByTeacherId(teacherId);
            model.addAttribute("theses", theses);
            
            // Count statistics
            long pendingProposals = proposalDtos.stream()
                .filter(p -> p.getStatus() == ThesisProposalStatus.PENDING)
                .count();
            long approvedProposals = proposalDtos.stream()
                .filter(p -> p.getStatus() == ThesisProposalStatus.APPROVED)
                .count();
            long successfulDefenses = thesisService.countSuccessfulDefensesByTeacher(teacherId);
            
            model.addAttribute("pendingCount", pendingProposals);
            model.addAttribute("approvedCount", approvedProposals);
            model.addAttribute("successfulDefenses", successfulDefenses);
            model.addAttribute("totalProposals", proposalDtos.size());
            model.addAttribute("totalTheses", theses.size());
            
            model.addAttribute("title", "Teacher Dashboard");
            return "teacher/dashboard";
        } catch (Exception e) {
            log.error("Error loading teacher dashboard", e);
            model.addAttribute("error", "Unable to load dashboard. Please try again.");
            return "error";
        }
    }

    @GetMapping("/proposals")
    public String proposals(Model model) {
        try {
            UUID teacherId = AuthenticationUtils.getCurrentUserId();
            List<ThesisProposalDto> proposalDtos = thesisApplicationService.getThesisProposalsByTeacherId(teacherId);
            List<ThesisProposalViewModel> proposals = proposalDtos.stream()
                .map(thesisApplicationMapper::toViewModel)
                .toList();
            
            model.addAttribute("proposals", proposals);
            model.addAttribute("title", "My Proposals");
            return "teacher/proposals";
        } catch (Exception e) {
            log.error("Error loading teacher proposals", e);
            model.addAttribute("error", "Unable to load proposals. Please try again.");
            return "error";
        }
    }

    @GetMapping("/proposals/create")
    public String createProposalForm(Model model) {
        model.addAttribute("proposal", new ThesisProposalViewModel());
        model.addAttribute("title", "Create New Proposal");
        return "teacher/create-proposal";
    }

    @PostMapping("/proposals/create")
    public String createProposal(@Valid @ModelAttribute("proposal") ThesisProposalViewModel proposalViewModel,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Create New Proposal");
            return "teacher/create-proposal";
        }
        
        try {
            UUID teacherId = AuthenticationUtils.getCurrentUserId();
            
            // Convert view model to DTO for service layer
            ThesisProposalDto proposalDto = thesisApplicationMapper.toDto(proposalViewModel);
            proposalDto.setTeacherId(teacherId);
            proposalDto.setStatus(ThesisProposalStatus.PENDING);
            
            ThesisProposalDto createdProposal = thesisApplicationService.createThesisProposal(proposalDto);
            
            redirectAttributes.addFlashAttribute("message", 
                "Proposal '" + createdProposal.getTitle() + "' created successfully!");
            return "redirect:/teacher/proposals";
        } catch (Exception e) {
            log.error("Error creating proposal", e);
            model.addAttribute("error", "Unable to create proposal. Please try again.");
            model.addAttribute("title", "Create New Proposal");
            return "teacher/create-proposal";
        }
    }

    @GetMapping("/theses")
    public String theses(Model model) {
        try {
            UUID teacherId = AuthenticationUtils.getCurrentUserId();
            List<ThesisDto> theses = thesisService.findThesesByTeacherId(teacherId);
            
            model.addAttribute("theses", theses);
            model.addAttribute("title", "Supervised Theses");
            return "teacher/theses";
        } catch (Exception e) {
            log.error("Error loading teacher theses", e);
            model.addAttribute("error", "Unable to load theses. Please try again.");
            return "error";
        }
    }

    @GetMapping("/proposal/{id}")
    public String proposalDetails(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            UUID teacherId = AuthenticationUtils.getCurrentUserId();
            ThesisProposalDto proposalDto = thesisApplicationService.getThesisProposalById(id);
            
            // Check if this proposal belongs to the current teacher
            if (!proposalDto.getTeacherId().equals(teacherId)) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to view this proposal.");
                return "redirect:/teacher/proposals";
            }
            
            ThesisProposalViewModel proposal = thesisApplicationMapper.toViewModel(proposalDto);
            model.addAttribute("proposal", proposal);
            model.addAttribute("title", "Proposal Details");
            return "teacher/proposal-details";
        } catch (Exception e) {
            log.error("Error loading proposal details", e);
            redirectAttributes.addFlashAttribute("error", "Unable to load proposal details. Please try again.");
            return "redirect:/teacher/proposals";
        }
    }

    @GetMapping("/thesis/{id}")
    public String thesisDetails(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ThesisDto thesis = thesisService.getThesisById(id);
            UUID teacherId = AuthenticationUtils.getCurrentUserId();
            
            // Check if this thesis belongs to the current teacher (through proposal)
            ThesisProposalDto proposal = thesisApplicationService.getThesisProposalById(thesis.getProposalId());
            if (!proposal.getTeacherId().equals(teacherId)) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to view this thesis.");
                return "redirect:/teacher/theses";
            }
            
            model.addAttribute("thesis", thesis);
            model.addAttribute("proposal", proposal);
            model.addAttribute("title", "Thesis Details");
            return "teacher/thesis-details";
        } catch (Exception e) {
            log.error("Error loading thesis details", e);
            redirectAttributes.addFlashAttribute("error", "Unable to load thesis details. Please try again.");
            return "redirect:/teacher/theses";
        }
    }

    @PostMapping("/proposal/{id}/status")
    public String updateProposalStatus(@PathVariable UUID id,
                                     @ModelAttribute("status") String status,
                                     RedirectAttributes redirectAttributes) {
        try {
            UUID teacherId = AuthenticationUtils.getCurrentUserId();
            ThesisProposalDto proposal = thesisApplicationService.getThesisProposalById(id);
            
            // Check if this proposal belongs to the current teacher
            if (!proposal.getTeacherId().equals(teacherId)) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to update this proposal.");
                return "redirect:/teacher/proposals";
            }
            
            ThesisProposalStatus newStatus = ThesisProposalStatus.valueOf(status.toUpperCase());
            thesisApplicationService.updateThesisProposalStatus(id, newStatus);
            
            redirectAttributes.addFlashAttribute("message", "Proposal status updated successfully!");
            return "redirect:/teacher/proposal/" + id;
        } catch (Exception e) {
            log.error("Error updating proposal status", e);
            redirectAttributes.addFlashAttribute("error", "Unable to update proposal status. Please try again.");
            return "redirect:/teacher/proposal/" + id;
        }
    }
}
