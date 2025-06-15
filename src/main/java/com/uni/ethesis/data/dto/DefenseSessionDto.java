package com.uni.ethesis.data.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefenseSessionDto {
    private UUID id;
    private OffsetDateTime dateAndTime;
    private String notes;
    private UUID thesisId;
    private UUID defenseId;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastModifiedAt;
}
