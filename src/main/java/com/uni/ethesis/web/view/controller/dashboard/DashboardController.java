package com.uni.ethesis.web.view.controller.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.uni.ethesis.data.dto.DefenseSessionDto;
import com.uni.ethesis.data.dto.DefenseSessionProfessorDto;
import com.uni.ethesis.data.dto.TeacherDto;
import com.uni.ethesis.data.dto.ThesisDto;
import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.data.dto.UserDto;
import com.uni.ethesis.enums.ThesisProposalStatus;
import com.uni.ethesis.enums.ThesisStatus;
import com.uni.ethesis.service.DefenseSessionProfessorService;
import com.uni.ethesis.service.DefenseSessionService;
import com.uni.ethesis.service.DepartmentService;
import com.uni.ethesis.service.TeacherService;
import com.uni.ethesis.service.ThesisProposalService;
import com.uni.ethesis.service.ThesisService;
import com.uni.ethesis.service.UserService;
import com.uni.ethesis.service.UserViewService;
import com.uni.ethesis.utils.mappers.DepartmentMapper;
import com.uni.ethesis.utils.mappers.TeacherMapper;
import com.uni.ethesis.utils.mappers.ThesisMapper;
import com.uni.ethesis.utils.mappers.ThesisProposalMapper;
import com.uni.ethesis.web.view.model.GradingSessionViewModel;
import com.uni.ethesis.web.view.model.ThesisViewModel;
import com.uni.ethesis.web.view.model.UserViewModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final ThesisProposalService thesisProposalService;
    private final ThesisService thesisService;
    private final UserViewService userViewService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final TeacherService teacherService;
    private final DepartmentMapper departmentMapper;
    private final TeacherMapper teacherMapper;
    private final ThesisMapper thesisMapper;
    private final ThesisProposalMapper thesisProposalMapper;
    private final DefenseSessionProfessorService defenseSessionProfessorService;
    private final DefenseSessionService defenseSessionService;
    public DashboardController(
            ThesisProposalService thesisProposalService,
            ThesisService thesisService,
            UserViewService userViewService,
            UserService userService,
            DepartmentService departmentService,
            TeacherService teacherService,
            DepartmentMapper departmentMapper,
            TeacherMapper teacherMapper,
            ThesisMapper thesisMapper,
            ThesisProposalMapper thesisProposalMapper,
            DefenseSessionProfessorService defenseSessionProfessorService,
            DefenseSessionService defenseSessionService) {
        this.thesisProposalService = thesisProposalService;
        this.thesisService = thesisService;
        this.userViewService = userViewService;
        this.userService = userService;
        this.departmentService = departmentService;
        this.teacherService = teacherService;
        this.departmentMapper = departmentMapper;
        this.teacherMapper = teacherMapper;
        this.thesisMapper = thesisMapper;
        this.thesisProposalMapper = thesisProposalMapper;
        this.defenseSessionProfessorService = defenseSessionProfessorService;
        this.defenseSessionService = defenseSessionService;
    }

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
                
                // Using the ThesisViewModel which is now available
                List<ThesisDto> supervisedTheses = thesisService.findThesesByTeacherId(teacherDto.getId());
                // Set count of supervised theses
                model.addAttribute("supervisedTheses", supervisedTheses.size());
                
                model.addAttribute("successfulDefensesCount", thesisService.countSuccessfulDefensesByTeacher(teacherDto.getId()));
                
                List<ThesisDto> thesesAwaitingReview = thesisService.findThesesAwaitingReview();
                List<ThesisViewModel> thesesAwaitingReviewViewModels = thesesAwaitingReview.stream()
                    .map(this::populateThesisViewModel)
                    .collect(Collectors.toList());
                model.addAttribute("thesesAwaitingReview", thesesAwaitingReviewViewModels);
                
                List<ThesisDto> thesesReadyForDefense = thesisService.findThesesReadyForDefense();
                List<ThesisViewModel> thesesReadyForDefenseViewModels = thesesReadyForDefense.stream()
                    .map(this::populateThesisViewModel)
                    .collect(Collectors.toList());
                model.addAttribute("thesesReadyForDefense", thesesReadyForDefenseViewModels);
                
                // Get defense sessions for this professor that need grading
                try {
                    UUID professorId = teacherDto.getId();
                    
                    // Get defense sessions where this professor is assigned
                    List<DefenseSessionProfessorDto> professorSessions = 
                            defenseSessionProfessorService.getDefenseSessionsByProfessor(professorId);
                    
                    // Map to view models similar to what's done in GradingController
                    List<GradingSessionViewModel> gradingSessionViewModels = new ArrayList<>();
                    
                    for (DefenseSessionProfessorDto professorSession : professorSessions) {
                        try {
                            UUID sessionId = professorSession.getDefenseSessionId();
                            DefenseSessionDto sessionDto = defenseSessionService.getDefenseSessionById(sessionId);
                            
                            if (sessionDto != null && sessionDto.getThesisId() != null) {
                                ThesisDto thesisDto = thesisService.getThesisById(sessionDto.getThesisId());
                                
                                if (thesisDto != null) {
                                    GradingSessionViewModel viewModel = new GradingSessionViewModel();
                                    viewModel.setId(sessionId);
                                    viewModel.setDateAndTime(sessionDto.getDateAndTime());
                                    viewModel.setGraded(professorSession.getGrade() != null);
                                    viewModel.setThesisId(sessionDto.getThesisId());
                                    
                                    // Get proposal information
                                    UUID proposalId = thesisDto.getProposalId();
                                    if (proposalId != null) {
                                        ThesisProposalDto proposalDto = thesisProposalService.getThesisProposalById(proposalId);
                                        if (proposalDto != null) {
                                            viewModel.setProposalTitle(proposalDto.getTitle());
                                            
                                            // Get student information
                                            UUID studentId = proposalDto.getStudentId();
                                            if (studentId != null) {
                                                UserDto studentDto = userService.getUserById(studentId);
                                                if (studentDto != null) {
                                                    viewModel.setStudentName(
                                                            studentDto.getFirstName() + " " + studentDto.getLastName());
                                                }
                                            }
                                        }
                                    }
                                    
                                    // Only add sessions that haven't been graded by this professor yet
                                    if (!viewModel.isGraded()) {
                                        gradingSessionViewModels.add(viewModel);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("Error mapping defense session {} to view model: {}", 
                                professorSession.getDefenseSessionId(), e.getMessage());
                        }
                    }
                    
                    // Add defense sessions to the model
                    model.addAttribute("defenseSessions", gradingSessionViewModels);
                    model.addAttribute("defensesNeedingGradingCount", gradingSessionViewModels.size());
                    model.addAttribute("gradingPageUrl", "/dashboard/grading");
                    
                } catch (Exception e) {
                    log.error("Error loading defense sessions for grading: {}", e.getMessage());
                    // Create default values for error case
                    model.addAttribute("defenseSessions", new ArrayList<>());
                    model.addAttribute("defensesNeedingGradingCount", 0);
                    model.addAttribute("gradingPageUrl", "/dashboard/grading");
                }
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
    
    /**
     * Helper method to populate a ThesisViewModel with data from a ThesisDto and its associated ThesisProposal.
     * 
     * @param thesisDto The source thesis DTO
     * @return A fully populated ThesisViewModel
     */
    private ThesisViewModel populateThesisViewModel(ThesisDto thesisDto) {
        // Start by mapping the base fields
        ThesisViewModel viewModel = thesisMapper.thesisDtoToViewModel(thesisDto);
        
        // Populate the missing information from the related thesis proposal
        if (thesisDto.getProposalId() != null) {
            try {
                ThesisProposalDto proposalDto = thesisProposalService.getThesisProposalById(thesisDto.getProposalId());
                if (proposalDto != null) {
                    // Set data from the proposal
                    viewModel.setTitle(proposalDto.getTitle());
                    viewModel.setGoal(proposalDto.getGoal());
                    viewModel.setObjectives(proposalDto.getObjectives());
                    viewModel.setTechnology(proposalDto.getTechnology());
                    viewModel.setStudentId(proposalDto.getStudentId().toString());
                    viewModel.setTeacherId(proposalDto.getTeacherId().toString());
                    viewModel.setDepartmentId(proposalDto.getDepartmentId().toString());
                    
                    // Get student name
                    if (proposalDto.getStudentId() != null) {
                        UserDto studentDto = userService.getUserById(proposalDto.getStudentId());
                        if (studentDto != null) {
                            viewModel.setStudentName(studentDto.getFirstName() + " " + studentDto.getLastName());
                        }
                    }
                    
                    // Get teacher name
                    if (proposalDto.getTeacherId() != null) {
                        UserDto teacherUserDto = userService.getUserById(proposalDto.getTeacherId());
                        if (teacherUserDto != null) {
                            viewModel.setTeacherName(teacherUserDto.getFirstName() + " " + teacherUserDto.getLastName());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error getting proposal for thesis {}: {}", thesisDto.getId(), e.getMessage());
            }
        }
        return viewModel;
    }
    

}
