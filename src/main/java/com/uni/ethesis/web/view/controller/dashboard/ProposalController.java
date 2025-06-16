package com.uni.ethesis.web.view.controller.dashboard;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.uni.ethesis.data.dto.DepartmentDto;
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
import com.uni.ethesis.service.UserService;
import com.uni.ethesis.service.UserViewService;
import com.uni.ethesis.utils.AuthenticationUtils;
import com.uni.ethesis.utils.mappers.DepartmentMapper;
import com.uni.ethesis.utils.mappers.TeacherMapper;
import com.uni.ethesis.utils.mappers.ThesisProposalMapper;
import com.uni.ethesis.web.view.model.CreateThesisProposalViewModel;
import com.uni.ethesis.web.view.model.DepartmentViewModel;
import com.uni.ethesis.web.view.model.TeacherViewModel;
import com.uni.ethesis.web.view.model.ThesisProposalViewModel;
import com.uni.ethesis.web.view.model.UpdateThesisProposalViewModel;
import com.uni.ethesis.web.view.model.UserViewModel;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/dashboard/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ThesisProposalService thesisProposalService;
    private final ThesisProposalMapper thesisProposalMapper;
    private final DepartmentService departmentService;
    private final TeacherService teacherService;
    private final UserViewService userViewService;
    private final DepartmentMapper departmentMapper;
    private final TeacherMapper teacherMapper;
    private final ThesisService thesisService;
    private final UserService userService;

    @GetMapping("/browse")
    @PreAuthorize("hasRole('STUDENT')")
    public String browseProposals(Model model, @RequestParam(required = false) String search) {
        try {
            List<ThesisProposalDto> proposalDtos;
            
            if (search != null && !search.trim().isEmpty()) {
                // For now, get all and filter - in a real app, you'd implement search in the service
                proposalDtos = thesisProposalService.getAllThesisProposals().stream()
                    .filter(p -> p.getStatus() == ThesisProposalStatus.PENDING)
                    .filter(p -> p.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                                p.getGoal().toLowerCase().contains(search.toLowerCase()) ||
                                p.getTechnology().toLowerCase().contains(search.toLowerCase()))
                    .toList();
                model.addAttribute("searchQuery", search);
            } else {
                // Get all pending proposals (available for application)
                proposalDtos = thesisProposalService.getThesisProposalsByStatus(ThesisProposalStatus.PENDING);
            }
            
            List<ThesisProposalViewModel> proposals = proposalDtos.stream()
                    .map(thesisProposalMapper::toViewModel)
                    .toList();
            model.addAttribute("proposals", proposals);
            model.addAttribute("title", "Browse Available Proposals");
            return "dashboard/proposals/browse";
        } catch (Exception e) {
            log.error("Error browsing proposals", e);
            model.addAttribute("error", "Unable to load proposals. Please try again.");
            return "error";
        }
    }

    // === PROPOSAL LISTING METHODS (moved from DashboardController) ===
    
    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public String showStudentProposals(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to access student proposals");
            return "redirect:/";
        }

        try {
            // Get user information using UserViewService
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", user);

            // Load all proposals for student (current)
            List<ThesisProposalDto> studentProposalDtos = thesisProposalService
                    .getThesisProposalsByStudentId(AuthenticationUtils.getCurrentUserId());
            
            List<ThesisProposalViewModel> studentProposals = studentProposalDtos.stream()
                    .map(thesisProposalMapper::toViewModel)
                    .toList();
            
            model.addAttribute("proposals", studentProposals);
            model.addAttribute("pageTitle", "My Proposals");
            model.addAttribute("pageDescription", "All your thesis proposals and their current status");

            log.info("Student proposals loaded for: {}", user.getEmail());
            return "dashboard/proposals/student-list";

        } catch (Exception e) {
            log.error("Error loading student proposals", e);
            model.addAttribute("error", "Failed to load your proposals");
            return "dashboard/main";
        }
    }

    @GetMapping("/current")
    @PreAuthorize("hasRole('STUDENT')")
    public String showStudentCurrentProposals(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to access current proposals");
            return "redirect:/";
        }

        try {
            // Get user information using UserViewService
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", user);

            // Load current (active) proposals for student - pending, approved, in_progress
            List<ThesisProposalDto> currentProposalDtos = thesisProposalService
                    .getThesisProposalsByStudentId(AuthenticationUtils.getCurrentUserId())
                    .stream()
                    .filter(p -> p.getStatus().name().equals("PENDING") || 
                               p.getStatus().name().equals("APPROVED") || 
                               p.getStatus().name().equals("IN_PROGRESS"))
                    .toList();
            
            List<ThesisProposalViewModel> currentProposals = currentProposalDtos.stream()
                    .map(thesisProposalMapper::toViewModel)
                    .toList();
            
            model.addAttribute("proposals", currentProposals);
            model.addAttribute("pageTitle", "Current Proposals");
            model.addAttribute("pageDescription", "Your active thesis proposals that are pending, approved or in progress");

            log.info("Current proposals loaded for student: {}", user.getEmail());
            return "dashboard/proposals/student-list";

        } catch (Exception e) {
            log.error("Error loading current proposals", e);
            model.addAttribute("error", "Failed to load current proposals");
            return "dashboard/main";
        }
    }

    @GetMapping("/past")
    @PreAuthorize("hasRole('STUDENT')")
    public String showStudentPastProposals(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to access past proposals");
            return "redirect:/";
        }

        try {
            // Get user information using UserViewService
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", user);

            // Get the student's thesis (if exists)
            try {
                ThesisDto studentThesis = thesisService.getThesisByStudentId(AuthenticationUtils.getCurrentUserId());
                
                // Check if thesis is in a "past" state (DEFENDED or FAILED)
                if (studentThesis != null && 
                    (studentThesis.getStatus() == ThesisStatus.DEFENDED || 
                     studentThesis.getStatus() == ThesisStatus.FAILED)) {
                    
                    // Get the proposal from the thesis
                    ThesisProposalDto proposalDto = thesisProposalService.getThesisProposalById(studentThesis.getProposalId());
                    ThesisProposalViewModel proposalViewModel = thesisProposalMapper.toViewModel(proposalDto);
                    
                    model.addAttribute("proposals", List.of(proposalViewModel));
                } else {
                    model.addAttribute("proposals", List.of());
                }
            } catch (Exception e) {
                // Student might not have a thesis yet
                log.debug("No thesis found for student: {}", user.getEmail());
                model.addAttribute("proposals", List.of());
            }
            
            model.addAttribute("pageTitle", "Past Theses");
            model.addAttribute("pageDescription", "Your completed theses that have been defended or failed");

            log.info("Past proposals loaded for student: {}", user.getEmail());
            return "dashboard/proposals/student-list";

        } catch (Exception e) {
            log.error("Error loading past proposals", e);
            model.addAttribute("error", "Failed to load past proposals");
            return "dashboard/main";
        }
    }

    // === PROPOSAL VIEW AND EDIT METHODS (moved from DashboardController) ===
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public String viewProposal(@PathVariable UUID id, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to access proposal view");
            return "redirect:/";
        }

        try {
            // Get user information using UserViewService
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", user);

            // Get the proposal
            ThesisProposalDto proposalDto;
            try {
                proposalDto = thesisProposalService.getThesisProposalById(id);
            } catch (Exception e) {
                log.warn("Proposal with id {} not found", id);
                model.addAttribute("error", "Proposal not found");
                return "dashboard/main";
            }
            
            ThesisProposalViewModel proposal = thesisProposalMapper.toViewModel(proposalDto);
            
            // Manually populate student and teacher names
            try {
                if (proposalDto.getStudentId() != null) {
                    UserDto studentUser = userService.getUserById(proposalDto.getStudentId());
                    proposal.setStudentName(studentUser.getFirstName() + " " + studentUser.getLastName());
                }
                if (proposalDto.getTeacherId() != null) {
                    UserDto teacherUser = userService.getUserById(proposalDto.getTeacherId());
                    proposal.setTeacherName(teacherUser.getFirstName() + " " + teacherUser.getLastName());
                }
                if (proposalDto.getDepartmentId() != null) {
                    DepartmentDto departmentDto = departmentService.getDepartmentById(proposalDto.getDepartmentId());
                    proposal.setDepartmentName(departmentDto.getName());
                }
            } catch (Exception e) {
                log.warn("Could not fetch participant names", e);
                // Fallback to IDs if user lookup fails
                if (proposalDto.getStudentId() != null) {
                    proposal.setStudentName("Student " + proposalDto.getStudentId().toString().substring(0, 8));
                }
                if (proposalDto.getTeacherId() != null) {
                    proposal.setTeacherName("Teacher " + proposalDto.getTeacherId().toString().substring(0, 8));
                }
                if (proposalDto.getDepartmentId() != null) {
                    proposal.setDepartmentName("Department " + proposalDto.getDepartmentId().toString().substring(0, 8));
                }
            }
            
            model.addAttribute("proposal", proposal);

            // Check if user has permission to view this proposal
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
                log.warn("User {} does not have permission to view proposal {}", user.getEmail(), id);
                model.addAttribute("error", "You don't have permission to view this proposal");
                return "dashboard/main";
            }

            // Check if thesis exists for this proposal (for upload functionality)
            boolean hasThesis = false;
            try {
                ThesisDto thesis = thesisService.getThesisByProposalId(id);
                hasThesis = true;
                model.addAttribute("thesis", thesis);
            } catch (Exception e) {
                // Thesis doesn't exist yet
                log.debug("No thesis found for proposal {}", id);
            }
            
            model.addAttribute("hasThesis", hasThesis);
            model.addAttribute("canUploadThesis", 
                proposalDto.getStatus().name().equals("APPROVED") && 
                user.getRole().contains("student") && 
                proposalDto.getStudentId().equals(currentUserId));

            log.info("Proposal {} viewed by user: {}", id, user.getEmail());
            return "dashboard/proposals/view";

        } catch (Exception e) {
            log.error("Error loading proposal view", e);
            model.addAttribute("error", "Failed to load proposal details");
            return "dashboard/main";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('STUDENT')")
    public String editProposal(@PathVariable UUID id, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to edit proposal");
            return "redirect:/";
        }

        try {
            // Get user information using UserViewService
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", user);

            // Get the proposal
            ThesisProposalDto proposalDto;
            try {
                proposalDto = thesisProposalService.getThesisProposalById(id);
            } catch (Exception e) {
                log.warn("Proposal with id {} not found for editing", id);
                model.addAttribute("error", "Proposal not found");
                return "redirect:/dashboard/proposals";
            }
            
            // Check if user owns this proposal
            UUID currentUserId = AuthenticationUtils.getCurrentUserId();
            if (!proposalDto.getStudentId().equals(currentUserId)) {
                log.warn("User {} does not own proposal {}", user.getEmail(), id);
                model.addAttribute("error", "You can only edit your own proposals");
                return "redirect:/dashboard/proposals";
            }

            // Check if proposal can be edited (only PENDING proposals can be edited)
            if (!proposalDto.getStatus().name().equals("PENDING")) {
                log.warn("User {} trying to edit non-pending proposal {}", user.getEmail(), id);
                model.addAttribute("error", "You can only edit proposals that are pending approval");
                return "redirect:/dashboard/proposals/" + id;
            }

            // Convert to UpdateViewModel for editing
            UpdateThesisProposalViewModel proposal = UpdateThesisProposalViewModel.builder()
                .id(id)
                .title(proposalDto.getTitle())
                .goal(proposalDto.getGoal())
                .objectives(proposalDto.getObjectives())
                .technology(proposalDto.getTechnology())
                .build();
            model.addAttribute("proposal", proposal);

            log.info("Proposal {} edit form loaded for user: {}", id, user.getEmail());
            return "dashboard/proposals/edit";

        } catch (Exception e) {
            log.error("Error loading proposal edit form", e);
            model.addAttribute("error", "Failed to load proposal for editing");
            return "redirect:/dashboard/proposals";
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('STUDENT')")
    public String updateProposal(@PathVariable UUID id, 
                               @ModelAttribute @Valid UpdateThesisProposalViewModel proposalViewModel,
                               BindingResult bindingResult,
                               Model model, 
                               Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to update proposal");
            return "redirect:/";
        }

        try {
            // Get user information
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", user);

            // Check if there are validation errors
            if (bindingResult.hasErrors()) {
                model.addAttribute("error", "Please correct the errors below");
                return "dashboard/proposals/edit";
            }

            // Get the existing proposal to verify ownership and status
            ThesisProposalDto existingProposal;
            try {
                existingProposal = thesisProposalService.getThesisProposalById(id);
            } catch (Exception e) {
                log.warn("Proposal with id {} not found for updating", id);
                model.addAttribute("error", "Proposal not found");
                return "redirect:/dashboard/proposals";
            }
            
            // Check ownership
            UUID currentUserId = AuthenticationUtils.getCurrentUserId();
            if (!existingProposal.getStudentId().equals(currentUserId)) {
                log.warn("User {} does not own proposal {}", user.getEmail(), id);
                model.addAttribute("error", "You can only edit your own proposals");
                return "redirect:/dashboard/proposals";
            }

            // Check if proposal can be edited
            if (!existingProposal.getStatus().name().equals("PENDING")) {
                log.warn("User {} trying to update non-pending proposal {}", user.getEmail(), id);
                model.addAttribute("error", "You can only edit proposals that are pending approval");
                return "redirect:/dashboard/proposals/" + id;
            }

            // Create DTO with updated fields from ViewModel
            ThesisProposalDto proposalDto = ThesisProposalDto.builder()
                .id(id)
                .title(proposalViewModel.getTitle())
                .goal(proposalViewModel.getGoal())
                .objectives(proposalViewModel.getObjectives())
                .technology(proposalViewModel.getTechnology())
                .studentId(currentUserId)
                .status(existingProposal.getStatus())
                .departmentId(existingProposal.getDepartmentId())
                .teacherId(existingProposal.getTeacherId())
                .build();
            
            // Update the proposal
            thesisProposalService.updateThesisProposal(id, proposalDto);
            
            log.info("Proposal {} updated successfully by user: {}", id, user.getEmail());
            return "redirect:/dashboard/proposals/" + id + "?updated=true";

        } catch (Exception e) {
            log.error("Error updating proposal", e);
            model.addAttribute("error", "Failed to update proposal. Please try again.");
            return "dashboard/proposals/edit";
        }
    }

    // === EXISTING METHODS ===
    @PostMapping("/{id}/apply")
    @PreAuthorize("hasRole('STUDENT')")
    public String applyForProposal(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UUID studentId = userViewService.getCurrentUser(auth).getId();
            ThesisProposalDto originalProposal = thesisProposalService.getThesisProposalById(id);
            
            // Check if student can apply
            List<ThesisProposalDto> studentProposals = thesisProposalService.getThesisProposalsByStudentId(studentId);
            boolean hasAppliedToTeacher = studentProposals.stream()
                .anyMatch(p -> p.getTeacherId().equals(originalProposal.getTeacherId()));
            
            if (hasAppliedToTeacher) {
                redirectAttributes.addFlashAttribute("error", "You have already applied to this teacher.");
                return "redirect:/dashboard/proposals/browse";
            }
            
            if (originalProposal.getStatus() != ThesisProposalStatus.PENDING) {
                redirectAttributes.addFlashAttribute("error", "This proposal is no longer available for application.");
                return "redirect:/dashboard/proposals/browse";
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
            
            ThesisProposalDto createdApplication = thesisProposalService.createThesisProposal(applicationDto);
            
            redirectAttributes.addFlashAttribute("success", 
                "Successfully applied for proposal: " + createdApplication.getTitle());
            return "redirect:/dashboard";
        } catch (Exception e) {
            log.error("Error applying for proposal", e);
            redirectAttributes.addFlashAttribute("error", "Unable to apply for proposal. Please try again.");
            return "redirect:/dashboard/proposals/" + id;
        }
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('STUDENT')")
    public String showCreateProposalForm(Model model) {
        try {
            // Get authentication from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Get current user's departments using UserViewService
            UUID currentUserId = userViewService.getCurrentUser(authentication).getId();
            List<DepartmentDto> userDepartmentDtos = departmentService.getDepartmentsByUserId(currentUserId);
            List<DepartmentViewModel> departments = departmentMapper.toViewModels(userDepartmentDtos);
            
            // Get all teachers
            List<TeacherDto> teacherDtos = teacherService.getAllTeachers();
            List<TeacherViewModel> teachers = teacherMapper.toViewModels(teacherDtos);
            
            model.addAttribute("createProposalForm", new CreateThesisProposalViewModel());
            model.addAttribute("departments", departments);
            model.addAttribute("teachers", teachers);
            model.addAttribute("title", "Create Thesis Proposal");
            
            return "dashboard/proposals/create";
        } catch (Exception e) {
            log.error("Error loading create proposal form", e);
            model.addAttribute("error", "Unable to load form. Please try again.");
            return "error";
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('STUDENT')")
    public String createProposal(@Valid @ModelAttribute("createProposalForm") CreateThesisProposalViewModel form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            // Get authentication from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (bindingResult.hasErrors()) {
                // Reload form data
                UUID currentUserId = userViewService.getCurrentUser(authentication).getId();
                List<DepartmentDto> userDepartments = departmentService.getDepartmentsByUserId(currentUserId);
                List<TeacherDto> teachers = teacherService.getAllTeachers();
                
                model.addAttribute("departments", userDepartments);
                model.addAttribute("teachers", teachers);
                model.addAttribute("title", "Create Thesis Proposal");
                return "dashboard/proposals/create";
            }

            UUID currentUserId = userViewService.getCurrentUser(authentication).getId();
            
            // Check if student already has a proposal with this teacher
            List<ThesisProposalDto> existingProposals = thesisProposalService.getThesisProposalsByStudentId(currentUserId);
            boolean hasExistingProposalWithTeacher = existingProposals.stream()
                .anyMatch(p -> p.getTeacherId().equals(UUID.fromString(form.getTeacherId())));
            
            if (hasExistingProposalWithTeacher) {
                bindingResult.rejectValue("teacherId", "error.teacherId", "You already have a proposal with this teacher");
                
                // Reload form data
                List<DepartmentDto> userDepartments = departmentService.getDepartmentsByUserId(currentUserId);
                List<TeacherDto> teachers = teacherService.getAllTeachers();
                
                model.addAttribute("departments", userDepartments);
                model.addAttribute("teachers", teachers);
                model.addAttribute("title", "Create Thesis Proposal");
                return "dashboard/proposals/create";
            }

            // Create the thesis proposal using mapper
            ThesisProposalDto proposalDto = thesisProposalMapper.fromCreateViewModel(form);
            proposalDto.setStudentId(currentUserId);
            proposalDto.setTeacherId(UUID.fromString(form.getTeacherId()));
            proposalDto.setDepartmentId(UUID.fromString(form.getDepartmentId()));
            proposalDto.setStatus(ThesisProposalStatus.PENDING);

            ThesisProposalDto createdProposal = thesisProposalService.createThesisProposal(proposalDto);
            
            log.info("Student {} created thesis proposal: {}", currentUserId, createdProposal.getTitle());
            redirectAttributes.addFlashAttribute("success", "Thesis proposal created successfully!");
            
            return "redirect:/dashboard";
        } catch (Exception e) {
            log.error("Error creating thesis proposal", e);
            redirectAttributes.addFlashAttribute("error", "Unable to create proposal. Please try again.");
            return "redirect:/dashboard/proposals/create";
        }
    }

    @PostMapping("/{id}/create-thesis")
    @PreAuthorize("hasRole('STUDENT')")
    public String createThesisFromProposal(@PathVariable UUID id, 
                                         RedirectAttributes redirectAttributes,
                                         Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to create thesis");
            return "redirect:/login";
        }

        try {
            // Get user information
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            UUID currentUserId = AuthenticationUtils.getCurrentUserId();

            // Get the proposal
            ThesisProposalDto proposalDto;
            try {
                proposalDto = thesisProposalService.getThesisProposalById(id);
            } catch (Exception e) {
                log.warn("Proposal with id {} not found for thesis creation", id);
                redirectAttributes.addFlashAttribute("error", "Proposal not found");
                return "redirect:/dashboard/proposals";
            }
            
            // Check if user owns this proposal
            if (!proposalDto.getStudentId().equals(currentUserId)) {
                log.warn("User {} does not own proposal {}", user.getEmail(), id);
                redirectAttributes.addFlashAttribute("error", "You can only create thesis from your own proposals");
                return "redirect:/dashboard/proposals/" + id;
            }

            // Check if proposal is approved
            if (!proposalDto.getStatus().name().equals("APPROVED")) {
                log.warn("User {} trying to create thesis from non-approved proposal {}", user.getEmail(), id);
                redirectAttributes.addFlashAttribute("error", "You can only create thesis from approved proposals");
                return "redirect:/dashboard/proposals/" + id;
            }

            // Check if thesis already exists for this proposal
            try {
                boolean existingThesis = thesisService.thesisExists(id);
                if (existingThesis) {
                    log.info("Thesis already exists for proposal {}, redirecting to thesis page", id);
                    return "redirect:/dashboard/" + id;
                }
            } catch (Exception e) {
                // Thesis doesn't exist yet, which is what we want
                log.debug("No existing thesis found for proposal {}, proceeding with creation", id);
            }

            // Create thesis from proposal
            ThesisDto createdThesis = thesisService.createThesis(id);
            
            log.info("Thesis {} created successfully from proposal {} by user: {}", 
                    createdThesis.getId(), id, user.getEmail());
            redirectAttributes.addFlashAttribute("success", "Thesis work started! You can now upload your document.");
            
            return "redirect:/dashboard/" + createdThesis.getId();

        } catch (Exception e) {
            log.error("Error creating thesis from proposal", e);
            redirectAttributes.addFlashAttribute("error", "Failed to start thesis work. Please try again.");
            return "redirect:/dashboard/proposals/" + id;
        }
    }
}
