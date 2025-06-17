package com.uni.ethesis.web.view.model;

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
public class GradingSessionViewModel {
    private UUID id;
    private OffsetDateTime dateAndTime;
    private UUID thesisId;
    private String studentName;
    private String proposalTitle;
    private boolean graded;
    private Integer grade;
    private String thoughts;
}
