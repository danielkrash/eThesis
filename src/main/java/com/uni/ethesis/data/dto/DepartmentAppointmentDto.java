package com.uni.ethesis.data.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentAppointmentDto {
    
    private UUID id;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Department ID is required")
    private UUID departmentId;
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    private LocalDateTime endDate; // null means current/indefinite appointment
    
    // Additional fields for display purposes
    private String userFullName;
    private String userEmail;
    private String departmentName;
    private boolean isCurrent; // computed field to indicate if this is the current appointment
    
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
}
