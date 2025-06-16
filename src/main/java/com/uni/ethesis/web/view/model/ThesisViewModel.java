package com.uni.ethesis.web.view.model;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThesisViewModel {

    private String id;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Goal cannot exceed 1000 characters")
    private String goal;

    @Size(max = 2000, message = "Objectives cannot exceed 2000 characters")
    private String objectives;

    @Size(max = 500, message = "Technology cannot exceed 500 characters")
    private String technology;

    private String studentId;
    private String teacherId;
    private String departmentId;
    private String status;
    private String pdfPath;

    // Display fields for the view
    private String studentName;
    private String teacherName;
    private String createdAt;
    private String lastModifiedAt;

    private Set<ThesisReviewViewModel> reviews;
}