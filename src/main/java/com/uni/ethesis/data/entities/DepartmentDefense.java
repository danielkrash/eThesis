package com.uni.ethesis.data.entities;

import jakarta.persistence.*;
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
@Table(name = "department_has_defenses")
public class DepartmentDefense extends BaseEntity{
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "defense_id", nullable = false)
    private Defense defense;
}
