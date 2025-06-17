package com.uni.ethesis.web.view.controller.dashboard;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.uni.ethesis.data.dto.DefenseDto;
import com.uni.ethesis.data.dto.DefenseSessionDto;
import com.uni.ethesis.data.dto.DefenseSessionProfessorDto;
import com.uni.ethesis.data.dto.DepartmentDto;
import com.uni.ethesis.data.dto.TeacherDto;
import com.uni.ethesis.data.dto.ThesisDto;
import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.data.dto.UserDto;
import com.uni.ethesis.enums.ThesisStatus;
import com.uni.ethesis.service.DefenseService;
import com.uni.ethesis.service.DefenseSessionProfessorService;
import com.uni.ethesis.service.DefenseSessionService;
import com.uni.ethesis.service.DepartmentService;
import com.uni.ethesis.service.TeacherService;
import com.uni.ethesis.service.ThesisProposalService;
import com.uni.ethesis.service.ThesisService;
import com.uni.ethesis.service.UserService;
import com.uni.ethesis.web.view.model.DefenseSessionViewModel;
import com.uni.ethesis.web.view.model.DefenseThesisViewModel;
import com.uni.ethesis.web.view.model.DefenseViewModel;

@Controller
@RequestMapping("/dashboard/defences")
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class DefensesController {

    private final DefenseService defenseService;
    private final DefenseSessionService defenseSessionService;
    private final DefenseSessionProfessorService defenseSessionProfessorService;
    private final DepartmentService departmentService;
    private final ThesisService thesisService;
    private final ThesisProposalService thesisProposalService;
    private final TeacherService teacherService;
    private final UserService userService;

    @Autowired
    public DefensesController(DefenseService defenseService,
                             DefenseSessionService defenseSessionService,
                             DefenseSessionProfessorService defenseSessionProfessorService,
                             DepartmentService departmentService,
                             ThesisService thesisService,
                             ThesisProposalService thesisProposalService,
                             TeacherService teacherService,
                             UserService userService) {
        this.defenseService = defenseService;
        this.defenseSessionService = defenseSessionService;
        this.defenseSessionProfessorService = defenseSessionProfessorService;
        this.departmentService = departmentService;
        this.thesisService = thesisService;
        this.thesisProposalService = thesisProposalService;
        this.teacherService = teacherService;
        this.userService = userService;
    }

    @GetMapping
    public String defencesPage(Model model, Authentication authentication) {
        UserDto currentUser = userService.getUserById(UUID.fromString(authentication.getName()));
        
        // Get all defense dates
        List<DefenseDto> defenses = defenseService.getAllDefenses();
        List<DefenseViewModel> allDefenseViewModels = defenses.stream()
                .map(this::mapToDefenseViewModel)
                .collect(Collectors.toList());
                
        // Separate future and past defenses
        LocalDate today = LocalDate.now();
        List<DefenseViewModel> futureDefenses = allDefenseViewModels.stream()
                .filter(defense -> defense.getDate().toLocalDate().isEqual(today) || defense.getDate().toLocalDate().isAfter(today))
                .collect(Collectors.toList());
                
        List<DefenseViewModel> pastDefenses = allDefenseViewModels.stream()
                .filter(defense -> defense.getDate().toLocalDate().isBefore(today))
                .collect(Collectors.toList());
        
        // Get all departments for the dropdown
        List<DepartmentDto> departments = departmentService.getAllDepartments();
        
        // Get all teachers for professor selection
        List<TeacherDto> teachers = teacherService.getAllTeachers();
        
        // Get theses waiting for defense (for assignment)
        List<ThesisDto> waitingTheses = thesisService.getThesesByStatus(ThesisStatus.WAITING_FOR_DEFENSE);
        List<DefenseThesisViewModel> waitingThesesViewModels = waitingTheses.stream()
                .map(this::mapToDefenseThesisViewModel)
                .collect(Collectors.toList());
        
        model.addAttribute("defenses", futureDefenses); // Keep original name for backward compatibility
        model.addAttribute("futureDefenses", futureDefenses);
        model.addAttribute("pastDefenses", pastDefenses);
        model.addAttribute("departments", departments);
        model.addAttribute("teachers", teachers);
        model.addAttribute("waitingTheses", waitingThesesViewModels);
        model.addAttribute("currentUser", currentUser);
        
        return "dashboard/defences/main";
    }

    @PostMapping("/create")
    public String createDefense(@RequestParam("date") String dateStr,
                               @RequestParam("location") String location,
                               @RequestParam("departmentIds") List<UUID> departmentIds,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication) {
        
        try {
            LocalDate localDate = LocalDate.parse(dateStr);
            
            // Validate that the date is in the future
            if (localDate.isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", "Defense date must be in the future");
                return "redirect:/dashboard/defences";
            }
            
            Date sqlDate = Date.valueOf(localDate);
            
            // Create defense
            DefenseDto defenseDto = DefenseDto.builder()
                    .date(sqlDate)
                    .location(location)
                    .build();
            
            defenseService.createDefense(defenseDto);
            
            // Associate departments with the defense
            // Note: This requires implementing the DepartmentDefense relationship
            // For now, we'll create the defense and handle department association separately
            
            redirectAttributes.addFlashAttribute("success", "Defense date created successfully");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create defense: " + e.getMessage());
        }
        
        return "redirect:/dashboard/defences";
    }

    @PostMapping("/assign-thesis")
    public String assignThesisToDefense(@RequestParam("thesisId") UUID thesisId,
                                       @RequestParam("defenseId") UUID defenseId,
                                       @RequestParam("defenseTime") String timeStr,
                                       @RequestParam("notes") String notes,
                                       @RequestParam("professorIds") List<UUID> professorIds,
                                       RedirectAttributes redirectAttributes) {
        
        try {
            // Get the defense date
            DefenseDto defense = defenseService.getDefenseById(defenseId);
            if (defense == null || defense.getDate() == null) {
                redirectAttributes.addFlashAttribute("error", "Invalid defense date selected");
                return "redirect:/dashboard/defences";
            }
            
            // Parse the time and combine with defense date
            java.time.LocalTime localTime = java.time.LocalTime.parse(timeStr);
            java.time.LocalDate defenseDate = defense.getDate().toLocalDate();
            java.time.LocalDateTime localDateTime = defenseDate.atTime(localTime);
            java.time.OffsetDateTime offsetDateTime = localDateTime.atOffset(java.time.ZoneOffset.UTC);
            
            // Validate that the datetime is in the future
            if (offsetDateTime.isBefore(java.time.OffsetDateTime.now())) {
                redirectAttributes.addFlashAttribute("error", "Defense time must be in the future");
                return "redirect:/dashboard/defences";
            }
            
            // Create defense session to link thesis with defense
            DefenseSessionDto defenseSessionDto = DefenseSessionDto.builder()
                    .thesisId(thesisId)
                    .defenseId(defenseId)
                    .dateAndTime(offsetDateTime)
                    .notes(notes)
                    .build();
            
            DefenseSessionDto createdSession = defenseSessionService.createDefenseSession(defenseSessionDto);
            
            // Create DefenseSessionProfessor entries for each selected professor
            for (UUID professorId : professorIds) {
                try {
                    defenseSessionProfessorService.addProfessorToDefenseSession(createdSession.getId(), professorId);
                } catch (Exception e) {
                    // Log error but continue with other professors
                    System.err.println("Failed to add professor " + professorId + " to session: " + e.getMessage());
                }
            }
            
            // Update thesis status to IN_DEFENSE_PROCESS
            ThesisDto thesis = thesisService.getThesisById(thesisId);
            thesis.setStatus(ThesisStatus.IN_DEFENSE_PROCESS);
            thesisService.updateThesis(thesisId, thesis);
            
            redirectAttributes.addFlashAttribute("success", 
                "Thesis assigned to defense successfully with " + professorIds.size() + " professors for " + 
                defenseDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")) + " at " + 
                localTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to assign thesis: " + e.getMessage());
        }
        
        return "redirect:/dashboard/defences";
    }

    private DefenseViewModel mapToDefenseViewModel(DefenseDto defenseDto) {
        // Get defense sessions for this defense
        List<DefenseSessionDto> sessions = defenseSessionService.getDefenseSessionsByDefenseId(defenseDto.getId());
        
        // Map sessions to view models
        List<DefenseSessionViewModel> sessionViewModels = sessions.stream()
                .map(this::mapToDefenseSessionViewModel)
                .collect(Collectors.toList());
        
        return DefenseViewModel.builder()
                .id(defenseDto.getId())
                .date(defenseDto.getDate())
                .location(defenseDto.getLocation())
                .sessionCount(sessions.size())
                .assignedThesesCount(sessions.size())
                .sessions(sessionViewModels)
                .createdAt(defenseDto.getCreatedAt())
                .lastModifiedAt(defenseDto.getLastModifiedAt())
                .build();
    }
    
    private DefenseSessionViewModel mapToDefenseSessionViewModel(DefenseSessionDto sessionDto) {
        DefenseThesisViewModel thesisViewModel = null;
        
        if (sessionDto.getThesisId() != null) {
            try {
                ThesisDto thesis = thesisService.getThesisById(sessionDto.getThesisId());
                thesisViewModel = mapToDefenseThesisViewModel(thesis);
            } catch (Exception e) {
                // Handle case where thesis might not be found
                thesisViewModel = null;
            }
        }
        
        // Get committee professors for this session
        List<String> committeeMembers;
        try {
            List<DefenseSessionProfessorDto> sessionProfessors = defenseSessionProfessorService.getProfessorsByDefenseSession(sessionDto.getId());
            committeeMembers = sessionProfessors.stream()
                    .map(sessionProf -> {
                        try {
                            TeacherDto teacher = teacherService.getTeacherById(sessionProf.getProfessorId());
                            return teacher.getFirstName() + " " + teacher.getLastName();
                        } catch (Exception e) {
                            return "Unknown Professor";
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Handle case where professors might not be found
            committeeMembers = java.util.Collections.emptyList();
        }
        
        return DefenseSessionViewModel.builder()
                .id(sessionDto.getId())
                .dateAndTime(sessionDto.getDateAndTime())
                .notes(sessionDto.getNotes())
                .thesisId(sessionDto.getThesisId())
                .defenseId(sessionDto.getDefenseId())
                .createdAt(sessionDto.getCreatedAt())
                .lastModifiedAt(sessionDto.getLastModifiedAt())
                .thesis(thesisViewModel)
                .committeeMembers(committeeMembers)
                .build();
    }
    
    private DefenseThesisViewModel mapToDefenseThesisViewModel(ThesisDto thesisDto) {
        DefenseThesisViewModel.DefenseThesisViewModelBuilder builder = DefenseThesisViewModel.builder()
                .id(thesisDto.getId())
                .pdfPath(thesisDto.getPdfPath())
                .finalGrade(thesisDto.getFinalGrade())
                .status(thesisDto.getStatus())
                .proposalId(thesisDto.getProposalId())
                .createdAt(thesisDto.getCreatedAt())
                .lastModifiedAt(thesisDto.getLastModifiedAt());
        
        // Get proposal details if available
        if (thesisDto.getProposalId() != null) {
            try {
                ThesisProposalDto proposal = thesisProposalService.getThesisProposalById(thesisDto.getProposalId());
                builder.proposalTitle(proposal.getTitle())
                       .proposalGoal(proposal.getGoal());
                
                // Get department name
                if (proposal.getDepartmentId() != null) {
                    try {
                        DepartmentDto department = departmentService.getDepartmentById(proposal.getDepartmentId());
                        builder.departmentName(department.getName());
                    } catch (Exception e) {
                        // Handle case where department might not be found
                    }
                }
                
                // Get student details
                if (proposal.getStudentId() != null) {
                    try {
                        UserDto student = userService.getUserById(proposal.getStudentId());
                        builder.studentFirstName(student.getFirstName())
                               .studentLastName(student.getLastName())
                               .studentEmail(student.getEmail());
                    } catch (Exception e) {
                        // Handle case where student might not be found
                    }
                }
                
                // Get teacher details
                if (proposal.getTeacherId() != null) {
                    try {
                        UserDto teacher = userService.getUserById(proposal.getTeacherId());
                        builder.teacherFirstName(teacher.getFirstName())
                               .teacherLastName(teacher.getLastName())
                               .teacherEmail(teacher.getEmail());
                    } catch (Exception e) {
                        // Handle case where teacher might not be found
                    }
                }
            } catch (Exception e) {
                // Handle case where proposal might not be found
            }
        }
        
        return builder.build();
    }
}
