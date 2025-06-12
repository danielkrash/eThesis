package com.uni.ethesis.data.entities;

import com.uni.ethesis.enums.ThesisStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Set;

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
    @OneToOne
    @JoinColumn(name = "proposal_id", unique = true)
    private ThesisProposal proposal;
    @OneToMany(mappedBy = "thesis", orphanRemoval = false)
    private Set<DefenseSession> sessions;
}
