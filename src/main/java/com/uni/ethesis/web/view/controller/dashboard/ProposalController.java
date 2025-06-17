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
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public String showProposals(Model model, Authentication auth, 
                               @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                               @RequestParam(required = false, defaultValue = "desc") String sortDir,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String search) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to access proposals");
            return "redirect:/";
        }

        try {
            // Get user information using UserViewService
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", user);

            List<ThesisProposalDto> proposalDtos;
            String pageTitle;
            String pageDescription;

            if (user.getRole().contains("student")) {
                // Load all proposals for student
                proposalDtos = thesisProposalService
                        .getThesisProposalsByStudentId(AuthenticationUtils.getCurrentUserId());
                pageTitle = "My Proposals";
                pageDescription = "All your thesis proposals and their current status";
            } else if (user.getRole().contains("teacher")) {
                // Load all proposals for teacher (supervised proposals)
                proposalDtos = thesisProposalService
                        .getThesisProposalsByTeacherId(AuthenticationUtils.getCurrentUserId());
                pageTitle = "All Supervised Proposals";
                pageDescription = "All thesis proposals you are supervising or have supervised";
            } else {
                // Admin can see all proposals
                proposalDtos = thesisProposalService.getAllThesisProposals();
                pageTitle = "All Proposals";
                pageDescription = "All thesis proposals in the system";
            }

            // Apply status filter if provided
            if (status != null && !status.trim().isEmpty()) {
                try {
                    ThesisProposalStatus statusEnum = ThesisProposalStatus.valueOf(status.toUpperCase());
                    proposalDtos = proposalDtos.stream()
                            .filter(p -> p.getStatus() == statusEnum)
                            .toList();
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid status filter: {}", status);
                }
            }

            // Apply search filter if provided
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                proposalDtos = proposalDtos.stream()
                        .filter(p -> p.getTitle().toLowerCase().contains(searchLower) ||
                                   p.getGoal().toLowerCase().contains(searchLower) ||
                                   (p.getTechnology() != null && p.getTechnology().toLowerCase().contains(searchLower)))
                        .toList();
            }

            // Sort proposals
            proposalDtos = sortProposals(proposalDtos, sortBy, sortDir);
            
            List<ThesisProposalViewModel> proposals = proposalDtos.stream()
                    .map(thesisProposalMapper::toViewModel)
                    .toList();

            // Populate additional information for all proposals
            for (ThesisProposalViewModel proposal : proposals) {
                ThesisProposalDto proposalDto = proposalDtos.stream()
                        .filter(dto -> dto.getId().toString().equals(proposal.getId()))
                        .findFirst()
                        .orElse(null);
                
                if (proposalDto != null) {
                    // Set student name
                    if (proposalDto.getStudentId() != null) {
                        try {
                            UserDto studentUser = userService.getUserById(proposalDto.getStudentId());
                            proposal.setStudentName(studentUser.getFirstName() + " " + studentUser.getLastName());
                        } catch (Exception e) {
                            proposal.setStudentName("Student " + proposalDto.getStudentId().toString().substring(0, 8));
                        }
                    }
                    
                    // Set teacher name
                    if (proposalDto.getTeacherId() != null) {
                        try {
                            UserDto teacherUser = userService.getUserById(proposalDto.getTeacherId());
                            proposal.setTeacherName(teacherUser.getFirstName() + " " + teacherUser.getLastName());
                        } catch (Exception e) {
                            proposal.setTeacherName("Teacher " + proposalDto.getTeacherId().toString().substring(0, 8));
                        }
                    }
                    
                    // Set department name
                    if (proposalDto.getDepartmentId() != null) {
                        try {
                            DepartmentDto departmentDto = departmentService.getDepartmentById(proposalDto.getDepartmentId());
                            proposal.setDepartmentName(departmentDto.getName());
                        } catch (Exception e) {
                            proposal.setDepartmentName("Unknown Department");
                        }
                    }
                    
                    // Set thesis information for APPROVED proposals
                    if (proposalDto.getStatus().name().equals("APPROVED") && proposalDto.getStudentId() != null) {
                        try {
                            ThesisDto thesisDto = thesisService.getThesisByProposalId(proposalDto.getId());
                            if (thesisDto != null) {
                                proposal.setThesisId(thesisDto.getId().toString());
                                proposal.setHasThesis(true);
                            } else {
                                proposal.setHasThesis(false);
                            }
                        } catch (Exception e) {
                            proposal.setHasThesis(false);
                        }
                    } else {
                        proposal.setHasThesis(false);
                    }
                }
            }
            
            model.addAttribute("proposals", proposals);
            model.addAttribute("pageTitle", pageTitle);
            model.addAttribute("pageDescription", pageDescription);
            
            // Add filter and sort parameters to model
            model.addAttribute("currentSortBy", sortBy);
            model.addAttribute("currentSortDir", sortDir);
            model.addAttribute("currentStatus", status);
            model.addAttribute("currentSearch", search);
            
            // Add available statuses for filter dropdown
            model.addAttribute("availableStatuses", ThesisProposalStatus.values());

            log.info("All proposals loaded for user: {} (role: {})", user.getEmail(), user.getRole());
            
            // Use different templates based on role for better UX
            if (user.getRole().contains("student")) {
                return "dashboard/proposals/student-list";
            } else {
                return "dashboard/proposals/teacher-list";
            }

        } catch (Exception e) {
            log.error("Error loading proposals", e);
            model.addAttribute("error", "Failed to load proposals");
            return "dashboard/main";
        }
    }

    private List<ThesisProposalDto> sortProposals(List<ThesisProposalDto> proposals, String sortBy, String sortDir) {
        java.util.Comparator<ThesisProposalDto> comparator;
        
        switch (sortBy.toLowerCase()) {
            case "title":
                comparator = java.util.Comparator.comparing(ThesisProposalDto::getTitle, String.CASE_INSENSITIVE_ORDER);
                break;
            case "status":
                comparator = java.util.Comparator.comparing(p -> p.getStatus().name());
                break;
            case "lastmodifiedat":
                comparator = java.util.Comparator.comparing(ThesisProposalDto::getLastModifiedAt, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
                break;
            case "createdat":
            default:
                comparator = java.util.Comparator.comparing(ThesisProposalDto::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
                break;
        }
        
        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        
        return proposals.stream()
                .sorted(comparator)
                .toList();
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public String showCurrentProposals(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to access current proposals");
            return "redirect:/";
        }

        try {
            // Get user information using UserViewService
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", user);

            List<ThesisProposalDto> currentProposalDtos;
            String pageTitle;
            String pageDescription;

            if (user.getRole().contains("student")) {
                // Load current (active) proposals for student - pending, approved, in_progress
                currentProposalDtos = thesisProposalService
                        .getThesisProposalsByStudentId(AuthenticationUtils.getCurrentUserId())
                        .stream()
                        .filter(p -> p.getStatus().name().equals("PENDING") || 
                                   p.getStatus().name().equals("APPROVED") || 
                                   p.getStatus().name().equals("IN_PROGRESS"))
                        .toList();
                pageTitle = "My Current Proposals";
                pageDescription = "Your active thesis proposals that are pending, approved or in progress";
            } else if (user.getRole().contains("teacher")) {
                // Load current (active) proposals for teacher - all approved and in_progress proposals they supervise
                currentProposalDtos = thesisProposalService
                        .getThesisProposalsByTeacherId(AuthenticationUtils.getCurrentUserId())
                        .stream()
                        .filter(p -> p.getStatus().name().equals("APPROVED") || 
                                   p.getStatus().name().equals("IN_PROGRESS"))
                        .toList();
                pageTitle = "Currently Supervised Proposals";
                pageDescription = "Thesis proposals you are currently supervising";
            } else {
                // Admin can see all current proposals
                currentProposalDtos = thesisProposalService
                        .getAllThesisProposals()
                        .stream()
                        .filter(p -> p.getStatus().name().equals("PENDING") || 
                                   p.getStatus().name().equals("APPROVED") || 
                                   p.getStatus().name().equals("IN_PROGRESS"))
                        .toList();
                pageTitle = "All Current Proposals";
                pageDescription = "All active thesis proposals in the system";
            }
            
            List<ThesisProposalViewModel> currentProposals = currentProposalDtos.stream()
                    .map(thesisProposalMapper::toViewModel)
                    .toList();
            
            // Populate additional information
            for (ThesisProposalViewModel proposal : currentProposals) {
                ThesisProposalDto proposalDto = currentProposalDtos.stream()
                        .filter(dto -> dto.getId().toString().equals(proposal.getId()))
                        .findFirst()
                        .orElse(null);
                
                if (proposalDto != null) {
                    // Set student name
                    if (proposalDto.getStudentId() != null) {
                        try {
                            UserDto studentUser = userService.getUserById(proposalDto.getStudentId());
                            proposal.setStudentName(studentUser.getFirstName() + " " + studentUser.getLastName());
                        } catch (Exception e) {
                            proposal.setStudentName("Student " + proposalDto.getStudentId().toString().substring(0, 8));
                        }
                    }
                    
                    // Set teacher name
                    if (proposalDto.getTeacherId() != null) {
                        try {
                            UserDto teacherUser = userService.getUserById(proposalDto.getTeacherId());
                            proposal.setTeacherName(teacherUser.getFirstName() + " " + teacherUser.getLastName());
                        } catch (Exception e) {
                            proposal.setTeacherName("Teacher " + proposalDto.getTeacherId().toString().substring(0, 8));
                        }
                    }
                    
                    // Set department name
                    if (proposalDto.getDepartmentId() != null) {
                        try {
                            DepartmentDto departmentDto = departmentService.getDepartmentById(proposalDto.getDepartmentId());
                            proposal.setDepartmentName(departmentDto.getName());
                        } catch (Exception e) {
                            proposal.setDepartmentName("Unknown Department");
                        }
                    }
                    
                    // Set thesis information for APPROVED proposals
                    if (proposalDto.getStatus().name().equals("APPROVED") && proposalDto.getStudentId() != null) {
                        try {
                            ThesisDto thesisDto = thesisService.getThesisByProposalId(proposalDto.getId());
                            if (thesisDto != null) {
                                proposal.setThesisId(thesisDto.getId().toString());
                                proposal.setHasThesis(true);
                            } else {
                                proposal.setHasThesis(false);
                            }
                        } catch (Exception e) {
                            proposal.setHasThesis(false);
                        }
                    } else {
                        proposal.setHasThesis(false);
                    }
                }
            }
            
            model.addAttribute("proposals", currentProposals);
            model.addAttribute("pageTitle", pageTitle);
            model.addAttribute("pageDescription", pageDescription);

            log.info("Current proposals loaded for user: {} (role: {})", user.getEmail(), user.getRole());
            return "dashboard/proposals/list";

        } catch (Exception e) {
            log.error("Error loading current proposals", e);
            model.addAttribute("error", "Failed to load current proposals");
            return "dashboard/main";
        }
    }

    @GetMapping("/past")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public String showPastProposals(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to access past proposals");
            return "redirect:/";
        }

        try {
            // Get user information using UserViewService
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", user);

            List<ThesisProposalViewModel> pastProposals = new java.util.ArrayList<>();
            String pageTitle;
            String pageDescription;

            if (user.getRole().contains("student")) {
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
                        pastProposals.add(proposalViewModel);
                    }
                } catch (Exception e) {
                    // Student might not have a thesis yet
                    log.debug("No thesis found for student: {}", user.getEmail());
                }
                pageTitle = "Past Theses";
                pageDescription = "Your completed theses that have been defended or failed";
                
            } else if (user.getRole().contains("teacher")) {
                // Get all theses supervised by this teacher that are completed
                List<ThesisDto> completedTheses = thesisService.findThesesByTeacherId(AuthenticationUtils.getCurrentUserId())
                        .stream()
                        .filter(thesis -> thesis.getStatus() == ThesisStatus.DEFENDED || 
                                        thesis.getStatus() == ThesisStatus.FAILED)
                        .toList();
                
                for (ThesisDto thesis : completedTheses) {
                    try {
                        ThesisProposalDto proposalDto = thesisProposalService.getThesisProposalById(thesis.getProposalId());
                        ThesisProposalViewModel proposalViewModel = thesisProposalMapper.toViewModel(proposalDto);
                        
                        // Set student name
                        if (proposalDto.getStudentId() != null) {
                            try {
                                UserDto studentUser = userService.getUserById(proposalDto.getStudentId());
                                proposalViewModel.setStudentName(studentUser.getFirstName() + " " + studentUser.getLastName());
                            } catch (Exception e) {
                                proposalViewModel.setStudentName("Student " + proposalDto.getStudentId().toString().substring(0, 8));
                            }
                        }
                        
                        pastProposals.add(proposalViewModel);
                    } catch (Exception e) {
                        log.warn("Could not load proposal for thesis {}: {}", thesis.getId(), e.getMessage());
                    }
                }
                pageTitle = "Past Supervised Theses";
                pageDescription = "Completed theses you have supervised that have been defended or failed";
                
            } else {
                // Admin can see all past theses
                List<ThesisDto> allCompletedTheses = thesisService.getAllTheses()
                        .stream()
                        .filter(thesis -> thesis.getStatus() == ThesisStatus.DEFENDED || 
                                        thesis.getStatus() == ThesisStatus.FAILED)
                        .toList();
                
                for (ThesisDto thesis : allCompletedTheses) {
                    try {
                        ThesisProposalDto proposalDto = thesisProposalService.getThesisProposalById(thesis.getProposalId());
                        ThesisProposalViewModel proposalViewModel = thesisProposalMapper.toViewModel(proposalDto);
                        
                        // Set student and teacher names
                        if (proposalDto.getStudentId() != null) {
                            try {
                                UserDto studentUser = userService.getUserById(proposalDto.getStudentId());
                                proposalViewModel.setStudentName(studentUser.getFirstName() + " " + studentUser.getLastName());
                            } catch (Exception e) {
                                proposalViewModel.setStudentName("Student " + proposalDto.getStudentId().toString().substring(0, 8));
                            }
                        }
                        
                        if (proposalDto.getTeacherId() != null) {
                            try {
                                UserDto teacherUser = userService.getUserById(proposalDto.getTeacherId());
                                proposalViewModel.setTeacherName(teacherUser.getFirstName() + " " + teacherUser.getLastName());
                            } catch (Exception e) {
                                proposalViewModel.setTeacherName("Teacher " + proposalDto.getTeacherId().toString().substring(0, 8));
                            }
                        }
                        
                        pastProposals.add(proposalViewModel);
                    } catch (Exception e) {
                        log.warn("Could not load proposal for thesis {}: {}", thesis.getId(), e.getMessage());
                    }
                }
                pageTitle = "All Past Theses";
                pageDescription = "All completed theses in the system that have been defended or failed";
            }
            
            model.addAttribute("proposals", pastProposals);
            model.addAttribute("pageTitle", pageTitle);
            model.addAttribute("pageDescription", pageDescription);

            log.info("Past proposals loaded for user: {} (role: {})", user.getEmail(), user.getRole());
            return "dashboard/proposals/list";

        } catch (Exception e) {
            log.error("Error loading past proposals", e);
            model.addAttribute("error", "Failed to load past proposals");
            return "dashboard/main";
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public String showPendingProposals(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to access pending proposals");
            return "redirect:/";
        }

        try {
            // Get user information using UserViewService
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", user);

            List<ThesisProposalDto> pendingProposalDtos;
            String pageTitle;
            String pageDescription;

            if (user.getRole().contains("teacher")) {
                // Load pending proposals assigned to this teacher for review
                pendingProposalDtos = thesisProposalService
                        .getThesisProposalsByTeacherId(AuthenticationUtils.getCurrentUserId())
                        .stream()
                        .filter(p -> p.getStatus() == ThesisProposalStatus.PENDING)
                        .toList();
                pageTitle = "Pending Proposals for Review";
                pageDescription = "Thesis proposals assigned to you that are awaiting your review and approval";
            } else {
                // Admin can see all pending proposals
                pendingProposalDtos = thesisProposalService
                        .getThesisProposalsByStatus(ThesisProposalStatus.PENDING);
                pageTitle = "All Pending Proposals";
                pageDescription = "All thesis proposals in the system awaiting review and approval";
            }
            
            List<ThesisProposalViewModel> pendingProposals = pendingProposalDtos.stream()
                    .map(thesisProposalMapper::toViewModel)
                    .toList();
            
            // Populate additional information
            for (ThesisProposalViewModel proposal : pendingProposals) {
                ThesisProposalDto proposalDto = pendingProposalDtos.stream()
                        .filter(dto -> dto.getId().toString().equals(proposal.getId()))
                        .findFirst()
                        .orElse(null);
                
                if (proposalDto != null) {
                    // Set student name
                    if (proposalDto.getStudentId() != null) {
                        try {
                            UserDto studentUser = userService.getUserById(proposalDto.getStudentId());
                            proposal.setStudentName(studentUser.getFirstName() + " " + studentUser.getLastName());
                        } catch (Exception e) {
                            proposal.setStudentName("Student " + proposalDto.getStudentId().toString().substring(0, 8));
                        }
                    }
                    
                    // Set teacher name
                    if (proposalDto.getTeacherId() != null) {
                        try {
                            UserDto teacherUser = userService.getUserById(proposalDto.getTeacherId());
                            proposal.setTeacherName(teacherUser.getFirstName() + " " + teacherUser.getLastName());
                        } catch (Exception e) {
                            proposal.setTeacherName("Teacher " + proposalDto.getTeacherId().toString().substring(0, 8));
                        }
                    }
                    
                    // Set department name
                    if (proposalDto.getDepartmentId() != null) {
                        try {
                            DepartmentDto departmentDto = departmentService.getDepartmentById(proposalDto.getDepartmentId());
                            proposal.setDepartmentName(departmentDto.getName());
                        } catch (Exception e) {
                            proposal.setDepartmentName("Unknown Department");
                        }
                    }
                    
                    // Set thesis information (though pending proposals won't have theses)
                    proposal.setHasThesis(false);
                }
            }
            
            model.addAttribute("proposals", pendingProposals);
            model.addAttribute("pageTitle", pageTitle);
            model.addAttribute("pageDescription", pageDescription);

            log.info("Pending proposals loaded for user: {} (role: {})", user.getEmail(), user.getRole());
            return "dashboard/proposals/list";

        } catch (Exception e) {
            log.error("Error loading pending proposals", e);
            model.addAttribute("error", "Failed to load pending proposals");
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
    public String showCreateProposalForm(Model model, RedirectAttributes redirectAttributes) {
        try {
            // Get authentication from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Get current user's departments using UserViewService
            UUID currentUserId = userViewService.getCurrentUser(authentication).getId();
            
            // Check if student has any active (PENDING or APPROVED) proposals
            List<ThesisProposalDto> existingProposals = thesisProposalService.getThesisProposalsByStudentId(currentUserId);
            
            // Check for active proposals (PENDING or APPROVED)
            boolean hasActiveProposal = existingProposals.stream()
                .anyMatch(p -> p.getStatus() == ThesisProposalStatus.PENDING || 
                              p.getStatus() == ThesisProposalStatus.APPROVED);
            
            if (hasActiveProposal) {
                redirectAttributes.addFlashAttribute("error", 
                    "You cannot create a new proposal while you have an active (pending or approved) proposal. Please wait until it's completed or rejected.");
                return "redirect:/dashboard";
            }
            
            // Additional check: if user has an approved proposal, check if associated thesis is still active
            boolean hasActiveThesis = existingProposals.stream()
                .filter(p -> p.getStatus() == ThesisProposalStatus.APPROVED)
                .anyMatch(p -> {
                    try {
                        // Check if there's an associated thesis and its status
                        ThesisDto thesis = thesisService.getThesisByProposalId(p.getId());
                        if (thesis != null) {
                            return thesis.getStatus() != ThesisStatus.DEFENDED && 
                                   thesis.getStatus() != ThesisStatus.FAILED;
                        }
                        return false; // No thesis found, safe to create new proposal
                    } catch (Exception e) {
                        log.warn("Could not check thesis status for proposal {}: {}", p.getId(), e.getMessage());
                        return false; // If we can't check, allow creation
                    }
                });
            
            if (hasActiveThesis) {
                redirectAttributes.addFlashAttribute("error", 
                    "You cannot create a new proposal while you have an active thesis work. Please complete your current thesis first.");
                return "redirect:/dashboard";
            }
            
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
            
            // Check if student has any active (PENDING or APPROVED) proposals
            List<ThesisProposalDto> existingProposals = thesisProposalService.getThesisProposalsByStudentId(currentUserId);
            
            // Check for active proposals (PENDING or APPROVED)
            boolean hasActiveProposal = existingProposals.stream()
                .anyMatch(p -> p.getStatus() == ThesisProposalStatus.PENDING || 
                              p.getStatus() == ThesisProposalStatus.APPROVED);
            
            if (hasActiveProposal) {
                bindingResult.rejectValue("teacherId", "error.activeProposal", 
                    "You cannot create a new proposal while you have an active (pending or approved) proposal. Please wait until it's completed or rejected.");
                
                // Reload form data
                List<DepartmentDto> userDepartments = departmentService.getDepartmentsByUserId(currentUserId);
                List<TeacherDto> teachers = teacherService.getAllTeachers();
                
                model.addAttribute("departments", userDepartments);
                model.addAttribute("teachers", teachers);
                model.addAttribute("title", "Create Thesis Proposal");
                return "dashboard/proposals/create";
            }
            
            // Additional check: if user has an approved proposal, check if associated thesis is still active
            boolean hasActiveThesis = existingProposals.stream()
                .filter(p -> p.getStatus() == ThesisProposalStatus.APPROVED)
                .anyMatch(p -> {
                    try {
                        // Check if there's an associated thesis and its status
                        ThesisDto thesis = thesisService.getThesisByProposalId(p.getId());
                        if (thesis != null) {
                            return thesis.getStatus() != ThesisStatus.DEFENDED && 
                                   thesis.getStatus() != ThesisStatus.FAILED;
                        }
                        return false; // No thesis found, safe to create new proposal
                    } catch (Exception e) {
                        log.warn("Could not check thesis status for proposal {}: {}", p.getId(), e.getMessage());
                        return false; // If we can't check, allow creation
                    }
                });
            
            if (hasActiveThesis) {
                bindingResult.rejectValue("teacherId", "error.activeThesis", 
                    "You cannot create a new proposal while you have an active thesis work. Please complete your current thesis first.");
                
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
                    return "redirect:/dashboard/thesis" + id;
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
            
            return "redirect:/dashboard/thesis" + createdThesis.getId();

        } catch (Exception e) {
            log.error("Error creating thesis from proposal", e);
            redirectAttributes.addFlashAttribute("error", "Failed to start thesis work. Please try again.");
            return "redirect:/dashboard/proposals/" + id;
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public String viewAllProposals(Model model, 
                                  @RequestParam(required = false) String sortBy,
                                  @RequestParam(required = false) String filterStatus,
                                  @RequestParam(required = false) String department,
                                  Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to access all proposals");
            return "redirect:/login";
        }

        try {
            // Get current user information
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", user);

            // Get all proposals from all departments
            List<ThesisProposalDto> allProposals = thesisProposalService.getAllThesisProposals();
            
            // Apply filters if specified
            if (filterStatus != null && !filterStatus.isEmpty() && !filterStatus.equals("all")) {
                try {
                    ThesisProposalStatus status = ThesisProposalStatus.valueOf(filterStatus);
                    allProposals = allProposals.stream()
                            .filter(proposal -> proposal.getStatus() == status)
                            .toList();
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid status filter: {}", filterStatus);
                }
            }
            
            // Filter by department if specified
            if (department != null && !department.isEmpty() && !department.equals("all")) {
                try {
                    UUID departmentId = UUID.fromString(department);
                    allProposals = allProposals.stream()
                            .filter(proposal -> proposal.getDepartmentId().equals(departmentId))
                            .toList();
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid department filter: {}", department);
                }
            }

            // Sort proposals
            if (sortBy != null && !sortBy.isEmpty()) {
                switch (sortBy) {
                    case "title":
                        allProposals = allProposals.stream()
                                .sorted((p1, p2) -> p1.getTitle().compareToIgnoreCase(p2.getTitle()))
                                .toList();
                        break;
                    case "status":
                        allProposals = allProposals.stream()
                                .sorted((p1, p2) -> p1.getStatus().toString().compareToIgnoreCase(p2.getStatus().toString()))
                                .toList();
                        break;
                    case "created":
                        allProposals = allProposals.stream()
                                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                                .toList();
                        break;
                    default:
                        // Default sort by created date (newest first)
                        allProposals = allProposals.stream()
                                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                                .toList();
                }
            } else {
                // Default sort by created date (newest first)
                allProposals = allProposals.stream()
                        .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                        .toList();
            }

            // Convert to ViewModels
            List<ThesisProposalViewModel> proposalViewModels = thesisProposalMapper.toViewModels(allProposals);
            
            // Populate additional information (student and teacher names)
            for (int i = 0; i < proposalViewModels.size(); i++) {
                ThesisProposalViewModel viewModel = proposalViewModels.get(i);
                ThesisProposalDto dto = allProposals.get(i);
                
                try {
                    // Get student name
                    if (dto.getStudentId() != null) {
                        UserDto student = userService.getUserById(dto.getStudentId());
                        viewModel.setStudentName(student.getFirstName() + " " + student.getLastName());
                    }
                    
                    // Get teacher name
                    if (dto.getTeacherId() != null) {
                        UserDto teacher = userService.getUserById(dto.getTeacherId());
                        viewModel.setTeacherName(teacher.getFirstName() + " " + teacher.getLastName());
                    }
                    
                    // Get department name
                    if (dto.getDepartmentId() != null) {
                        DepartmentDto dept = departmentService.getDepartmentById(dto.getDepartmentId());
                        viewModel.setDepartmentName(dept.getName());
                    }
                } catch (Exception e) {
                    log.warn("Could not load names for proposal {}: {}", viewModel.getId(), e.getMessage());
                }
            }

            model.addAttribute("proposals", proposalViewModels);
            
            // Get all departments for the filter dropdown
            List<DepartmentDto> departments = departmentService.getAllDepartments();
            List<DepartmentViewModel> departmentViewModels = departmentMapper.toViewModels(departments);
            model.addAttribute("departments", departmentViewModels);
            
            // Add filter parameters to maintain state
            model.addAttribute("currentSortBy", sortBy != null ? sortBy : "created");
            model.addAttribute("currentFilterStatus", filterStatus != null ? filterStatus : "all");
            model.addAttribute("currentDepartment", department != null ? department : "all");
            
            // Add proposal status options for filtering
            model.addAttribute("proposalStatuses", ThesisProposalStatus.values());
            
            log.info("All proposals page accessed by user: {} (Role: {})", user.getEmail(), user.getRole());
            return "dashboard/proposals/all-proposals";

        } catch (Exception e) {
            log.error("Error loading all proposals page", e);
            model.addAttribute("error", "Failed to load proposals. Please try again.");
            return "dashboard/main";
        }
    }

    @PostMapping("/all/{proposalId}/approve")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public String approveProposal(@PathVariable UUID proposalId,
                                 RedirectAttributes redirectAttributes,
                                 Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to approve proposal");
            return "redirect:/login";
        }

        try {
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            
            // Get the proposal
            ThesisProposalDto proposal = thesisProposalService.getThesisProposalById(proposalId);
            
            // Check if proposal is in PENDING status
            if (proposal.getStatus() != ThesisProposalStatus.PENDING) {
                redirectAttributes.addFlashAttribute("error", "Only pending proposals can be approved");
                return "redirect:/dashboard/proposals/all";
            }
            
            // Update status to APPROVED
            proposal.setStatus(ThesisProposalStatus.APPROVED);
            thesisProposalService.updateThesisProposal(proposalId, proposal);
            
            log.info("Proposal {} approved by user: {}", proposalId, user.getEmail());
            redirectAttributes.addFlashAttribute("success", "Proposal approved successfully!");
            return "redirect:/dashboard/proposals/all";

        } catch (Exception e) {
            log.error("Error approving proposal {}", proposalId, e);
            redirectAttributes.addFlashAttribute("error", "Failed to approve proposal. Please try again.");
            return "redirect:/dashboard/proposals/all";
        }
    }

    @PostMapping("/all/{proposalId}/reject")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public String rejectProposal(@PathVariable UUID proposalId,
                                @RequestParam(required = false) String reason,
                                RedirectAttributes redirectAttributes,
                                Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to reject proposal");
            return "redirect:/login";
        }

        try {
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            
            // Get the proposal
            ThesisProposalDto proposal = thesisProposalService.getThesisProposalById(proposalId);
            
            // Check if proposal is in PENDING status
            if (proposal.getStatus() != ThesisProposalStatus.PENDING) {
                redirectAttributes.addFlashAttribute("error", "Only pending proposals can be rejected");
                return "redirect:/dashboard/proposals/all";
            }
            
            // Update status to REJECTED
            proposal.setStatus(ThesisProposalStatus.REJECTED);
            
            // Add rejection reason if provided (assuming there's a field for this)
            // Note: You might need to add a rejectionReason field to ThesisProposalDto if it doesn't exist
            
            thesisProposalService.updateThesisProposal(proposalId, proposal);
            
            log.info("Proposal {} rejected by user: {} with reason: {}", proposalId, user.getEmail(), reason);
            redirectAttributes.addFlashAttribute("success", "Proposal rejected successfully!");
            return "redirect:/dashboard/proposals/all";

        } catch (Exception e) {
            log.error("Error rejecting proposal {}", proposalId, e);
            redirectAttributes.addFlashAttribute("error", "Failed to reject proposal. Please try again.");
            return "redirect:/dashboard/proposals/all";
        }
    }

    @PostMapping("/pending/{proposalId}/approve")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public String approvePendingProposal(@PathVariable UUID proposalId,
                                        RedirectAttributes redirectAttributes,
                                        Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to approve proposal");
            return "redirect:/login";
        }

        try {
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            UUID currentUserId = AuthenticationUtils.getCurrentUserId();
            
            // Get the proposal
            ThesisProposalDto proposal = thesisProposalService.getThesisProposalById(proposalId);
            
            // Check if proposal is in PENDING status
            if (proposal.getStatus() != ThesisProposalStatus.PENDING) {
                redirectAttributes.addFlashAttribute("error", "Only pending proposals can be approved");
                return "redirect:/dashboard/proposals/pending";
            }
            
            // Check if teacher has permission to approve this proposal (either assigned teacher or admin)
            if (user.getRole().contains("teacher") && !proposal.getTeacherId().equals(currentUserId)) {
                redirectAttributes.addFlashAttribute("error", "You can only approve proposals assigned to you");
                return "redirect:/dashboard/proposals/pending";
            }
            
            // Update status to APPROVED
            proposal.setStatus(ThesisProposalStatus.APPROVED);
            thesisProposalService.updateThesisProposal(proposalId, proposal);
            
            log.info("Proposal {} approved by user: {}", proposalId, user.getEmail());
            redirectAttributes.addFlashAttribute("success", "Proposal approved successfully!");
            return "redirect:/dashboard/proposals/pending";

        } catch (Exception e) {
            log.error("Error approving proposal {}", proposalId, e);
            redirectAttributes.addFlashAttribute("error", "Failed to approve proposal. Please try again.");
            return "redirect:/dashboard/proposals/pending";
        }
    }

    @PostMapping("/pending/{proposalId}/reject")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public String rejectPendingProposal(@PathVariable UUID proposalId,
                                       @RequestParam(required = false) String reason,
                                       RedirectAttributes redirectAttributes,
                                       Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to reject proposal");
            return "redirect:/login";
        }

        try {
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            UUID currentUserId = AuthenticationUtils.getCurrentUserId();
            
            // Get the proposal
            ThesisProposalDto proposal = thesisProposalService.getThesisProposalById(proposalId);
            
            // Check if proposal is in PENDING status
            if (proposal.getStatus() != ThesisProposalStatus.PENDING) {
                redirectAttributes.addFlashAttribute("error", "Only pending proposals can be rejected");
                return "redirect:/dashboard/proposals/pending";
            }
            
            // Check if teacher has permission to reject this proposal (either assigned teacher or admin)
            if (user.getRole().contains("teacher") && !proposal.getTeacherId().equals(currentUserId)) {
                redirectAttributes.addFlashAttribute("error", "You can only reject proposals assigned to you");
                return "redirect:/dashboard/proposals/pending";
            }
            
            // Update status to REJECTED
            proposal.setStatus(ThesisProposalStatus.REJECTED);
            
            // Add rejection reason if provided (you might need to add a rejectionReason field)
            // proposal.setRejectionReason(reason);
            
            thesisProposalService.updateThesisProposal(proposalId, proposal);
            
            log.info("Proposal {} rejected by user: {} with reason: {}", proposalId, user.getEmail(), reason);
            redirectAttributes.addFlashAttribute("success", "Proposal rejected successfully!");
            return "redirect:/dashboard/proposals/pending";

        } catch (Exception e) {
            log.error("Error rejecting proposal {}", proposalId, e);
            redirectAttributes.addFlashAttribute("error", "Failed to reject proposal. Please try again.");
            return "redirect:/dashboard/proposals/pending";
        }
    }
}
