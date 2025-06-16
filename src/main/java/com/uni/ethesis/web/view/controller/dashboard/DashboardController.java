package com.uni.ethesis.web.view.controller.dashboard;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.uni.ethesis.data.dto.TeacherDto;
import com.uni.ethesis.data.dto.ThesisDto;
import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.data.dto.UserDto;
import com.uni.ethesis.enums.ThesisProposalStatus;
import com.uni.ethesis.enums.ThesisStatus;
import com.uni.ethesis.service.DepartmentService;
import com.uni.ethesis.service.TeacherService;
import com.uni.ethesis.service.ThesisProposalService;
import com.uni.ethesis.service.ThesisService;
import com.uni.ethesis.service.UserViewService;
import com.uni.ethesis.utils.mappers.DepartmentMapper;
import com.uni.ethesis.utils.mappers.TeacherMapper;
import com.uni.ethesis.utils.mappers.ThesisMapper;
import com.uni.ethesis.utils.mappers.ThesisProposalMapper;
import com.uni.ethesis.web.view.model.UserViewModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final ThesisProposalService thesisProposalService;
    private final ThesisService thesisService;
    private final UserViewService userViewService;
    private final DepartmentService departmentService;
    private final TeacherService teacherService;
    private final DepartmentMapper departmentMapper;
    private final TeacherMapper teacherMapper;
    private final ThesisMapper thesisMapper;
    private final ThesisProposalMapper thesisProposalMapper;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public String showDashboard(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("User is not authenticated, redirecting to login.");
            return "redirect:/login";
        }

        try {
            UserViewModel userViewModel = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", userViewModel);
            UserDto currentUser = userViewService.getCurrentUser(auth);

            String userRoleString = userViewService.getUserRole(auth);

            if (userRoleString.contains("admin")) {
                loadAdminData(model);
            } else if (userRoleString.contains("teacher")) {
                loadTeacherData(model, currentUser);
            } else if (userRoleString.contains("student")) {
                loadStudentData(model, currentUser);
            }

            return "dashboard/main";
        } catch (Exception e) {
            log.error("Error loading dashboard for user {}: {}", auth.getName(), e.getMessage(), e);
            model.addAttribute("error", "Failed to load dashboard data. Please try again later or contact support.");
            return "dashboard/main";
        }
    }

    private void loadStudentData(Model model, UserDto currentUser) {
        try {
            List<ThesisProposalDto> proposalDtos = thesisProposalService.getThesisProposalsByStudentId(currentUser.getId());
            model.addAttribute("myProposals", thesisProposalMapper.toViewModels(proposalDtos));

            model.addAttribute("proposalCount", proposalDtos.size());
            long pendingCount = proposalDtos.stream().filter(p -> p.getStatus() == ThesisProposalStatus.PENDING).count();
            long approvedCount = proposalDtos.stream().filter(p -> p.getStatus() == ThesisProposalStatus.APPROVED).count();
            long rejectedCount = proposalDtos.stream().filter(p -> p.getStatus() == ThesisProposalStatus.REJECTED).count();
            
            model.addAttribute("pendingCount", (int) pendingCount);
            model.addAttribute("approvedCount", (int) approvedCount);
            model.addAttribute("rejectedCount", (int) rejectedCount);

            ThesisDto thesisDto = thesisService.getThesisByStudentId(currentUser.getId());
            if (thesisDto != null) {
                model.addAttribute("studentThesis", thesisMapper.thesisDtoToViewModel(thesisDto));
            }
        } catch (Exception e) {
            log.error("Failed to load student-specific data for user {}: {}", currentUser.getId(), e.getMessage(), e);
            model.addAttribute("studentDataError", "Could not load all student details.");
        }
    }

    private void loadTeacherData(Model model, UserDto currentUser) {
        try {
            TeacherDto teacherDto = teacherService.getTeacherById(currentUser.getId());
            if (teacherDto == null) {
                teacherDto = teacherService.getTeacherByEmail(currentUser.getEmail());
            }

            if (teacherDto != null) {
                List<ThesisProposalDto> teacherProposals = thesisProposalService.getThesisProposalsByTeacherId(teacherDto.getId());
                model.addAttribute("teacherProposals", thesisProposalMapper.toViewModels(teacherProposals));
                
                // Add count attributes for teacher dashboard statistics
                long pendingCount = teacherProposals.stream().filter(p -> p.getStatus() == ThesisProposalStatus.PENDING).count();
                long approvedCount = teacherProposals.stream().filter(p -> p.getStatus() == ThesisProposalStatus.APPROVED).count();
                long rejectedCount = teacherProposals.stream().filter(p -> p.getStatus() == ThesisProposalStatus.REJECTED).count();
                
                model.addAttribute("pendingCount", (int) pendingCount);
                model.addAttribute("approvedCount", (int) approvedCount);
                model.addAttribute("rejectedCount", (int) rejectedCount);
                
                // TODO: Uncomment when ThesisViewModel is implemented
                // model.addAttribute("supervisedTheses", thesisMapper.toViewModels(thesisService.findThesesByTeacherId(teacherDto.getId())));
                model.addAttribute("successfulDefensesCount", thesisService.countSuccessfulDefensesByTeacher(teacherDto.getId()));
                // TODO: Uncomment when ThesisViewModel is implemented
                // model.addAttribute("thesesAwaitingReview", thesisMapper.toViewModels(thesisService.findThesesAwaitingReview()));
                // model.addAttribute("thesesReadyForDefense", thesisMapper.toViewModels(thesisService.findThesesReadyForDefense()));
            } else {
                log.warn("Teacher data could not be loaded for user {} (ID: {}) as TeacherDto was not found.", currentUser.getEmail(), currentUser.getId());
                model.addAttribute("teacherDataError", "Could not load teacher-specific details.");
                // Set default counts to avoid template errors
                model.addAttribute("pendingCount", 0);
                model.addAttribute("approvedCount", 0);
                model.addAttribute("rejectedCount", 0);
            }
        } catch (Exception e) {
            log.error("Failed to load teacher-specific data for user {}: {}", currentUser.getId(), e.getMessage(), e);
            model.addAttribute("teacherDataError", "Could not load all teacher details.");
            // Set default counts to avoid template errors
            model.addAttribute("pendingCount", 0);
            model.addAttribute("approvedCount", 0);
            model.addAttribute("rejectedCount", 0);
        }
    }

    private void loadAdminData(Model model) {
        try {
            List<ThesisProposalDto> allProposals = thesisProposalService.getAllThesisProposals();
            model.addAttribute("allProposals", thesisProposalMapper.toViewModels(allProposals));
            // TODO: Uncomment when ThesisViewModel is implemented
            // model.addAttribute("allTheses", thesisMapper.toViewModels(thesisService.getAllTheses()));
            model.addAttribute("allDepartments", departmentMapper.toViewModels(departmentService.getAllDepartments()));
            model.addAttribute("allTeachers", teacherMapper.toViewModels(teacherService.getAllTeachers()));

            // Calculate counts from the retrieved data for consistency
            long pendingCount = allProposals.stream().filter(p -> p.getStatus() == ThesisProposalStatus.PENDING).count();
            long approvedCount = allProposals.stream().filter(p -> p.getStatus() == ThesisProposalStatus.APPROVED).count();
            long rejectedCount = allProposals.stream().filter(p -> p.getStatus() == ThesisProposalStatus.REJECTED).count();
            
            model.addAttribute("pendingProposalsCount", (int) pendingCount);
            model.addAttribute("approvedProposalsCount", (int) approvedCount);
            model.addAttribute("rejectedProposalsCount", (int) rejectedCount);
            
            // Also set the general count attributes used in templates
            model.addAttribute("pendingCount", (int) pendingCount);
            model.addAttribute("approvedCount", (int) approvedCount);
            model.addAttribute("rejectedCount", (int) rejectedCount);
            
            model.addAttribute("thesesAwaitingReviewCount", thesisService.findThesesAwaitingReview().size());
            model.addAttribute("thesesReadyForDefenseCount", thesisService.findThesesReadyForDefense().size());
            model.addAttribute("defendedThesesCount", thesisService.countThesesByStatus(ThesisStatus.DEFENDED));
        } catch (Exception e) {
            log.error("Failed to load admin-specific data: {}", e.getMessage(), e);
            model.addAttribute("adminDataError", "Could not load all administrative details.");
            // Set default counts to avoid template errors
            model.addAttribute("pendingCount", 0);
            model.addAttribute("approvedCount", 0);
            model.addAttribute("rejectedCount", 0);
            model.addAttribute("pendingProposalsCount", 0);
            model.addAttribute("approvedProposalsCount", 0);
            model.addAttribute("rejectedProposalsCount", 0);
        }
    }
    
    // === PROPOSAL REDIRECT MAPPINGS ===
    
    @GetMapping("/proposal/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public String viewProposal(@PathVariable UUID id) {
        // Redirect to the plural proposals controller
        return "redirect:/dashboard/proposals/" + id;
    }

    @GetMapping("/proposal/{id}/edit")
    @PreAuthorize("hasRole('STUDENT')")
    public String editProposal(@PathVariable UUID id) {
        // Redirect to the plural proposals controller
        return "redirect:/dashboard/proposals/" + id + "/edit";
    }
}
