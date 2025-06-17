package com.uni.ethesis.web.view.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.uni.ethesis.enums.ThesisStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefenseThesisViewModel {
    private UUID id;
    private String pdfPath;
    private BigDecimal finalGrade;
    private ThesisStatus status;
    private UUID proposalId;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastModifiedAt;
    
    // Proposal details for display
    private String proposalTitle;
    private String proposalGoal;
    
    // Student details
    private String studentFirstName;
    private String studentLastName;
    private String studentEmail;
    
    // Teacher/Supervisor details
    private String teacherFirstName;
    private String teacherLastName;
    private String teacherEmail;
    
    // Department details
    private String departmentName;
    
    // Computed properties
    public String getStudentFullName() {
        return (studentFirstName != null ? studentFirstName : "") + " " + 
               (studentLastName != null ? studentLastName : "");
    }
    
    public String getTeacherFullName() {
        return (teacherFirstName != null ? teacherFirstName : "") + " " + 
               (teacherLastName != null ? teacherLastName : "");
    }
}
