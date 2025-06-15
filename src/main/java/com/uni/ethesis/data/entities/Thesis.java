package com.uni.ethesis.data.entities;

import java.math.BigDecimal;
import java.util.Set;

import com.uni.ethesis.enums.ThesisStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
@Table(name = "theses")
public class Thesis extends BaseEntity {
    public static final BigDecimal BIG_DECIMAL = BigDecimal.valueOf(6.0);
    @Column(name = "pdf_path" , columnDefinition = "text")
    private String pdfPath;
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "6.0", inclusive = true)
    @Column(precision = 2, scale = 1 , name = "final_grade")
    private BigDecimal finalGrade;
    @Column(name = "status" , columnDefinition = "text")
    @Enumerated(EnumType.STRING)
    private ThesisStatus status;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "proposal_id", unique = true)
    private ThesisProposal proposal;
    @OneToMany(mappedBy = "thesis", orphanRemoval = true , cascade = CascadeType.ALL)
    private Set<DefenseSession> sessions;
}
