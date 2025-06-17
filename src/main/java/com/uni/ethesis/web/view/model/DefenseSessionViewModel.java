package com.uni.ethesis.web.view.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefenseSessionViewModel {
    private UUID id;
    private OffsetDateTime dateAndTime;
    private String notes;
    private UUID thesisId;
    private UUID defenseId;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastModifiedAt;
    
    // Thesis information for display
    private DefenseThesisViewModel thesis;
    
    // Committee members for display
    private List<String> committeeMembers;
}
