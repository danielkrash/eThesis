package com.uni.ethesis.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "users")
@Entity
public class User extends BaseEntity{
    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column(unique = true)
    private String email;
}
