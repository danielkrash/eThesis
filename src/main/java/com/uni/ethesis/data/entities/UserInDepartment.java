package com.uni.ethesis.data.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserInDepartment extends BaseEntity{
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    // This defines the Many-to-One relationship to the Department entity.
    // Many appointments can be made for one department (over time).
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}
