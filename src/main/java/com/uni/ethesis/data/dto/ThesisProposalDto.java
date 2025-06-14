package com.uni.ethesis.data.dto;

import java.util.UUID;

import com.uni.ethesis.enums.ThesisProposalStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThesisProposalDto {

    private UUID id;

    @NotBlank(message = "Title cannot be blank")
    private String title;

    private String goal;

    private String objectives;

    private String technology;

    @NotNull(message = "Student ID cannot be null")
    private UUID studentId;

    @NotNull(message = "Teacher ID cannot be null")
    private UUID teacherId;

    @NotNull(message = "Status cannot be null")
    private ThesisProposalStatus status;
}
