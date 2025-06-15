package com.uni.ethesis.data.entities;

import java.time.OffsetDateTime;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "defense_sessions")
public class DefenseSession extends BaseEntity {
    // @FutureOrPresent(message = "Date and time must be in the future or present")
    @Column(name = "date_and_time", nullable = true)
    private OffsetDateTime dateAndTime;
    @Column(columnDefinition = "text")
    private String notes;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "thesis_id", nullable = false)
    private Thesis thesis;
    @OneToMany(mappedBy = "defenseSession", orphanRemoval = false , cascade = CascadeType.ALL)
    private Set<DefenseSessionProfessor> professors;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "defense_id" , nullable = false)
    private Defense defense;
}
