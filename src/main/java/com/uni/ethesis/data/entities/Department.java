package com.uni.ethesis.data.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "departments")
public class Department extends BaseEntity {
    @Column(columnDefinition = "text")
    private String name;
    @Column(columnDefinition = "text")
    private String description;
    @OneToMany(mappedBy = "department", orphanRemoval = true , cascade = CascadeType.ALL)
    private Set<DepartmentAppointment> appointments;
    @OneToMany(mappedBy = "department", orphanRemoval = true , cascade = CascadeType.ALL)
    private Set<UserInDepartment> users;
    @OneToMany(mappedBy = "department", orphanRemoval = true , cascade = CascadeType.ALL)
    private Set<DepartmentDefense> defenses;
    @OneToMany(mappedBy = "department" , orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<ThesisProposal> proposals;
}
