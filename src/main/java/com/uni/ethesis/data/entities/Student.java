package com.uni.ethesis.data.entities;

import java.util.Set;

import org.hibernate.annotations.Check;

import com.uni.ethesis.enums.StudentType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "students", indexes = {
        @Index(name = "idx_student_university_id", columnList = "universityId", unique = true)
})
@Check(constraints = "university_id ~* '^f[0-9]{6}$'")
public class Student extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId
    @JoinColumn(name = "id")
    private User user;
    @Pattern(regexp = "(?i)f\\d{6}$")
    @Column(name = "university_id", nullable = false, unique = true, columnDefinition = "text")
    private String universityId;
    @Column(columnDefinition = "text")
    @Enumerated(EnumType.STRING)
    private StudentType studentType;
    @OneToMany(mappedBy = "student", orphanRemoval = false , cascade = CascadeType.ALL)
    private Set<ThesisProposal> studentThesisProposals;
}
