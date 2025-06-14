package com.uni.ethesis.data.entities;

import com.uni.ethesis.utils.DefenseSessionProfessorKey;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.Builder; // Added for @Builder.Default

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
    @ManyToOne
    @MapsId("defenseSessionId")
    @JoinColumn(name = "defense_session_id")
    private DefenseSession defenseSession;
    @ManyToOne
    @MapsId("professorId")
    @JoinColumn(name = "professor_id")
    private Teacher professor;
    @Column(name = "grade")
    @Min(0)
    @Max(100)
    private int grade; // 0 - 100
    @Column(name = "thoughts" , columnDefinition = "text")
    private String thoughts;
}
