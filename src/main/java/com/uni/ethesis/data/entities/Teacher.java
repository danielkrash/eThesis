package com.uni.ethesis.data.entities;

import com.uni.ethesis.enums.TeacherPosition;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "teachers")
public class Teacher extends BaseEntity {
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;
    @Column(columnDefinition = "text")
    @Enumerated(EnumType.STRING)
    private TeacherPosition position;
    @OneToMany(mappedBy = "teacher", orphanRemoval = false)
    private Set<ThesisProposal> teacherThesisProposals;
    @OneToMany(mappedBy = "teacher", orphanRemoval = false)
    private Set<Review> reviews;
    @OneToMany(mappedBy = "professor", orphanRemoval = false)
    private Set<DefenseSessionProfessor> defenseSessions;
}
