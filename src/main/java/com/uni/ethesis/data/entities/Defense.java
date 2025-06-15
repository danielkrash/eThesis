package com.uni.ethesis.data.entities;

import java.sql.Date;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
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
@Table(name = "defenses")
public class Defense extends BaseEntity {
     @Column(columnDefinition = "text")
     private String location;
     // @FutureOrPresent(message = "Date must be in the future or present")
     @Column(columnDefinition = "date" , nullable = true)
     private Date date;
     @OneToMany(mappedBy = "defense", orphanRemoval = false , cascade = CascadeType.ALL)
     private Set<DepartmentDefense> defenses;
     @OneToMany(mappedBy = "defense" , cascade = CascadeType.ALL)
     private Set<DefenseSession> defenseSessions;
}
