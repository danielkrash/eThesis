package com.uni.ethesis.data.entities;

import jakarta.persistence.Entity;
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
@Table(name = "thesis_proposal")
@SuperBuilder
public class ThesisProposal extends BaseEntity{
    private String title;
    private String goal;
    private String objectives;
    private String technology;
}
