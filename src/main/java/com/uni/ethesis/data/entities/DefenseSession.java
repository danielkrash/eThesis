package com.uni.ethesis.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jdk.jfr.Enabled;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "defense_sessions")
public class DefenseSession extends BaseEntity {
    @FutureOrPresent(message = "Date and time must be in the future or present")
    private OffsetDateTime dateAndTime;
    @Column(columnDefinition = "text")
    private String notes;
    @ManyToOne
    @JoinColumn(name = "thesis_id", nullable = false)
    private Thesis thesis;
    @OneToMany(mappedBy = "defenseSession", orphanRemoval = false)
    private Set<DefenseSessionProfessor> professors;
    @ManyToOne
    @JoinColumn(name = "defense_id" , nullable = false)
    private Defense defense;
}
