package com.uni.ethesis.web.view.model;

import java.sql.Date;
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
public class DefenseViewModel {
    private UUID id;
    private Date date;
    private String location;
    private int sessionCount;
    private int assignedThesesCount;
    private List<DefenseSessionViewModel> sessions;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastModifiedAt;
}
