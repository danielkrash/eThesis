package com.uni.ethesis.data.entities;

import com.uni.ethesis.enums.StudentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Check;

import java.util.Set;

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
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;
    @Pattern(regexp = "(?i)f\\d{6}$")
    @Column(name = "university_id", nullable = false, unique = true, columnDefinition = "text")
    private String universityId;
    @Column(columnDefinition = "text")
    @Enumerated(EnumType.STRING)
    private StudentType studentType;
    @OneToMany(mappedBy = "student", orphanRemoval = false)
    private Set<ThesisProposal> studentThesisProposals;
}
