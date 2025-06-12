package com.uni.ethesis.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comments")
public class Comment extends BaseEntity {
    @Column(columnDefinition = "text" , nullable = false)
    @Size(min = 1)
    private String content;
    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
