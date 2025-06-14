package com.uni.ethesis.data.dto;

import java.sql.Date;
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
public class DefenseDto {
    private UUID id;
    private String location;
    private Date date;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastModifiedAt;
}
