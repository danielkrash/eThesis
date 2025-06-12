package com.uni.ethesis.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.sql.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "defenses")
public class Defense extends BaseEntity {
     @Column(columnDefinition = "text")
     private String location;
     @FutureOrPresent(message = "Date must be in the future or present")
     @Column(columnDefinition = "date")
     private Date date;
     @OneToMany(mappedBy = "defense", orphanRemoval = false)
     private Set<DepartmentDefense> defenses;
     @OneToMany(mappedBy = "defense")
     private Set<DefenseSession> defenseSessions;
}
