package com.uni.ethesis.web.view.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.service.ThesisApplicationService;
import com.uni.ethesis.service.UserViewService;
import com.uni.ethesis.utils.AuthenticationUtils;
import com.uni.ethesis.utils.mappers.ThesisApplicationMapper;
import com.uni.ethesis.web.view.model.ThesisProposalViewModel;
import com.uni.ethesis.web.view.model.UserViewModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ThesisApplicationService thesisApplicationService;
    private final UserViewService userViewService;
    private final ThesisApplicationMapper thesisApplicationMapper;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public String showDashboard(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to access dashboard");
            return "redirect:/";
        }

        try {
            // Get user information using UserViewService
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", user);

            // Add role-specific data based on user's role(s)
            String userRole = user.getRole();
            if (userRole != null) {
                if (userRole.contains("student")) {
                    loadStudentData(model);
                }
                if (userRole.contains("teacher")) {
                    loadTeacherData(model);
                }
                if (userRole.contains("admin")) {
                    loadAdminData(model);
                }
            }

            // Add common dashboard data
            loadCommonData(model);

            log.info("Dashboard loaded for user: {} with role: {}", user.getEmail(), user.getRole());
            return "dashboard/main";

        } catch (Exception e) {
            log.error("Error loading dashboard", e);
            model.addAttribute("error", "Failed to load dashboard data");
            return "dashboard/main";
        }
    }

    private void loadStudentData(Model model) {
        try {
            // Get current user's proposals
            if (AuthenticationUtils.getCurrentUserId() != null) {
                List<ThesisProposalDto> myProposalDtos = thesisApplicationService
                        .getThesisProposalsByStudentId(AuthenticationUtils.getCurrentUserId());
                List<ThesisProposalViewModel> myProposals = myProposalDtos.stream()
                        .map(thesisApplicationMapper::toViewModel)
                        .toList();
                model.addAttribute("myProposals", myProposals);
                model.addAttribute("proposalCount", myProposals.size());
            }

            // Get available proposals from teachers
            List<ThesisProposalDto> availableProposalDtos = thesisApplicationService.getAllThesisProposals();
            List<ThesisProposalViewModel> availableProposals = availableProposalDtos.stream()
                    .map(thesisApplicationMapper::toViewModel)
                    .toList();
            model.addAttribute("availableProposals", availableProposals);

        } catch (Exception e) {
            log.error("Error loading student data", e);
            model.addAttribute("studentDataError", "Failed to load student data");
        }
    }

    private void loadTeacherData(Model model) {
        try {
            // Get teacher's proposals
            if (AuthenticationUtils.getCurrentUserId() != null) {
                List<ThesisProposalDto> myProposalDtos = thesisApplicationService
                        .getThesisProposalsByTeacherId(AuthenticationUtils.getCurrentUserId());
                List<ThesisProposalViewModel> myProposals = myProposalDtos.stream()
                        .map(thesisApplicationMapper::toViewModel)
                        .toList();
                model.addAttribute("myProposals", myProposals);
                model.addAttribute("proposalCount", myProposals.size());

                // Count proposals by status
                long pendingCount = myProposalDtos.stream()
                        .filter(p -> p.getStatus().name().equals("PENDING"))
                        .count();
                long approvedCount = myProposalDtos.stream()
                        .filter(p -> p.getStatus().name().equals("APPROVED"))
                        .count();
                
                model.addAttribute("pendingCount", pendingCount);
                model.addAttribute("approvedCount", approvedCount);
            }

        } catch (Exception e) {
            log.error("Error loading teacher data", e);
            model.addAttribute("teacherDataError", "Failed to load teacher data");
        }
    }

    private void loadAdminData(Model model) {
        try {
            // Get all proposals for admin overview
            List<ThesisProposalDto> allProposalDtos = thesisApplicationService.getAllThesisProposals();
            List<ThesisProposalViewModel> allProposals = allProposalDtos.stream()
                    .map(thesisApplicationMapper::toViewModel)
                    .toList();
            model.addAttribute("allProposals", allProposals);
            model.addAttribute("totalProposals", allProposals.size());

            // Statistics
            long pendingCount = allProposalDtos.stream()
                    .filter(p -> p.getStatus().name().equals("PENDING"))
                    .count();
            long approvedCount = allProposalDtos.stream()
                    .filter(p -> p.getStatus().name().equals("APPROVED"))
                    .count();
            long rejectedCount = allProposalDtos.stream()
                    .filter(p -> p.getStatus().name().equals("REJECTED"))
                    .count();

            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("approvedCount", approvedCount);
            model.addAttribute("rejectedCount", rejectedCount);

        } catch (Exception e) {
            log.error("Error loading admin data", e);
            model.addAttribute("adminDataError", "Failed to load admin data");
        }
    }

    private void loadCommonData(Model model) {
        // Add any common data that all users should see
        model.addAttribute("currentTime", java.time.LocalDateTime.now());
    }
}
