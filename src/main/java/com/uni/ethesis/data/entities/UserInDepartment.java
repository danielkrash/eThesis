package com.uni.ethesis.data.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "users_in_departments")
public class UserInDepartment extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    // This defines the Many-to-One relationship to the Department entity.
    // Many appointments can be made for one department (over time).
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}
