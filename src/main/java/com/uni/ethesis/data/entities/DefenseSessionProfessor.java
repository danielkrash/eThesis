package com.uni.ethesis.data.entities;

import com.uni.ethesis.utils.DefenseSessionProfessorKey;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max; // Added for @Builder.Default
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "defense_session_professors")
public class DefenseSessionProfessor extends AuditableEntity {
    @EmbeddedId
    @Builder.Default // Ensures the ID is initialized even with SuperBuilder
    private DefenseSessionProfessorKey id = new DefenseSessionProfessorKey(); // Initialize the embedded ID
    @ManyToOne(cascade = CascadeType.ALL)
    @MapsId("defenseSessionId")
    @JoinColumn(name = "defense_session_id")
    private DefenseSession defenseSession;
    @ManyToOne(cascade = CascadeType.ALL)
    @MapsId("professorId")
    @JoinColumn(name = "professor_id")
    private Teacher professor;
    @Column(name = "grade")
    @Min(0)
    @Max(100)
    private Integer grade; // Changed from int to Integer
    @Column(name = "thoughts" , columnDefinition = "text")
    private String thoughts;
}
