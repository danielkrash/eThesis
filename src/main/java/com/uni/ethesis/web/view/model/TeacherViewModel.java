package com.uni.ethesis.web.view.model;

import java.util.UUID;

import com.uni.ethesis.enums.TeacherPosition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherViewModel {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private TeacherPosition position;
    
    // Computed property for display
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
