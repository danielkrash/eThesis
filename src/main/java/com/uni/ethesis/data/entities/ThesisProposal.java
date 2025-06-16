package com.uni.ethesis.data.entities;

import com.uni.ethesis.enums.ThesisProposalStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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
@Table(name = "thesis_proposals")
@SuperBuilder
public class ThesisProposal extends BaseEntity {
    @Column(columnDefinition = "text")
    private String title;
    @Column(columnDefinition = "text")
    private String goal;
    @Column(columnDefinition = "text")
    private String objectives;
    @Column(columnDefinition = "text")
    private String technology;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "student_id")
    private Student student;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "text")
    private ThesisProposalStatus status;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "department_id")
    private Department department;
    @OneToOne(mappedBy = "proposal" , cascade = CascadeType.ALL)
    private Thesis thesis;
}
