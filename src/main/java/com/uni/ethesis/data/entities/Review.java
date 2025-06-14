package com.uni.ethesis.data.entities;

import com.uni.ethesis.enums.ReviewConclusion;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

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
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;
    @OneToMany(mappedBy = "review", orphanRemoval = false)
    private Set<Comment> comments;
}
