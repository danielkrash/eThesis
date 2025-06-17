package com.uni.ethesis.web.view.model;

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
public class DefenseCreateViewModel {
    private String date;
    private String location;
    private List<UUID> departmentIds;
}
