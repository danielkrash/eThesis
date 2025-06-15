package com.uni.ethesis.data.entities;

import java.util.Set;

import com.uni.ethesis.enums.TeacherPosition;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "teachers")
public class Teacher extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId
    @JoinColumn(name = "id")
    private User user;
    @Column(columnDefinition = "text")
    @Enumerated(EnumType.STRING)
    private TeacherPosition position;
    @OneToMany(mappedBy = "teacher", orphanRemoval = false , cascade = CascadeType.ALL)
    private Set<ThesisProposal> teacherThesisProposals;
    @OneToMany(mappedBy = "teacher", orphanRemoval = false , cascade = CascadeType.ALL)
    private Set<Review> reviews;
    @OneToMany(mappedBy = "professor", orphanRemoval = false , cascade = CascadeType.ALL)
    private Set<DefenseSessionProfessor> defenseSessions;
}
