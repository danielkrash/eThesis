package com.uni.ethesis.data.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.uni.ethesis.enums.ThesisStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThesisDto {
    private UUID id;
    private String pdfPath;
    private BigDecimal finalGrade;
    private ThesisStatus status;
    private UUID proposalId;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastModifiedAt;
}
