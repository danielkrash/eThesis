package com.uni.ethesis.data.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
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
public class CommentDto {
    private UUID id;
    
    @NotBlank(message = "Comment content cannot be blank")
    @Size(min = 1, max = 2000, message = "Comment content must be between 1 and 2000 characters")
    private String content;
    
    @NotNull(message = "Review ID is required")
    private UUID reviewId;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    private String userFullName;
    private String userRole; // "TEACHER" or "STUDENT"
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
