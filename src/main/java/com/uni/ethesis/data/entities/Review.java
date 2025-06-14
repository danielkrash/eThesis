package com.uni.ethesis.data.entities;

import java.util.Set;

import com.uni.ethesis.enums.ReviewConclusion;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "reviews")
public class Review extends BaseEntity {
    @Column(columnDefinition = "text", nullable = false)
    private String content;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "text", nullable = false)
    private ReviewConclusion conclusion;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "thesis_id", nullable = false)
    private Thesis thesis;
    @OneToMany(mappedBy = "review", orphanRemoval = false , cascade = CascadeType.ALL)
    private Set<Comment> comments;
}
