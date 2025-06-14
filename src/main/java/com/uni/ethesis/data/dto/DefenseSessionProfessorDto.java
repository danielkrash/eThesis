package com.uni.ethesis.data.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefenseSessionProfessorDto {
    
    @NotNull(message = "Defense session ID is required")
    private UUID defenseSessionId;
    
    @NotNull(message = "Professor ID is required")
    private UUID professorId;
    
    @Min(value = 0, message = "Grade must be between 0 and 100")
    @Max(value = 100, message = "Grade must be between 0 and 100")
    private Integer grade;
    
    @Size(max = 2000, message = "Thoughts must not exceed 2000 characters")
    private String thoughts;
    
    // Additional fields for display purposes
    private String professorFullName;
    private String professorPosition;
    private LocalDateTime defenseSessionDateTime;
    private String thesisTitle;
    private String studentName;
    
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
}
