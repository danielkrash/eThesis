package com.uni.ethesis.web.view.model;

import java.util.UUID;

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
public class UpdateThesisProposalViewModel {

    private UUID id;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Goal cannot be blank")
    @Size(max = 1000, message = "Goal cannot exceed 1000 characters")
    private String goal;

    @NotBlank(message = "Objectives cannot be blank")
    @Size(max = 2000, message = "Objectives cannot exceed 2000 characters")
    private String objectives;

    @NotBlank(message = "Technology cannot be blank")
    @Size(max = 500, message = "Technology cannot exceed 500 characters")
    private String technology;
}
