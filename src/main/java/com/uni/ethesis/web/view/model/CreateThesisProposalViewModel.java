package com.uni.ethesis.web.view.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateThesisProposalViewModel {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Goal is required")
    @Size(max = 1000, message = "Goal cannot exceed 1000 characters")
    private String goal;

    @NotBlank(message = "Objectives are required")
    @Size(max = 2000, message = "Objectives cannot exceed 2000 characters")
    private String objectives;

    @Size(max = 500, message = "Technology cannot exceed 500 characters")
    private String technology;

    @NotNull(message = "Department is required")
    private String departmentId;

    @NotNull(message = "Teacher is required")
    private String teacherId;

    // Helper fields for form display
    private String departmentName;
    private String teacherName;
}
