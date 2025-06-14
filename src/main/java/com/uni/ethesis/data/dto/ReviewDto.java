package com.uni.ethesis.data.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.uni.ethesis.enums.ReviewConclusion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private UUID id;
    private String content;
    private ReviewConclusion conclusion;
    private UUID teacherId;
    private UUID thesisId;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastModifiedAt;
}
