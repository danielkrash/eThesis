package com.uni.ethesis.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Fetch;
import org.springframework.data.repository.cdi.Eager;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
@SuperBuilder
@Entity
public class User extends BaseEntity {
    @Column(name = "first_name" , columnDefinition = "text")
    private String firstName;
    @Column(name = "last_name" , columnDefinition = "text")
    private String lastName;
    @Column(unique = true, name = "email" , columnDefinition = "text")
    @Email(message = "Email should be valid")
    private String email;
    @OneToMany(mappedBy = "user", orphanRemoval = false , fetch = FetchType.LAZY)
    private Set<DepartmentAppointment> appointments;
    @OneToMany(mappedBy = "user", orphanRemoval = false , fetch = FetchType.LAZY)
    private Set<UserInDepartment> departments;
    @OneToOne(mappedBy = "user" , orphanRemoval = false)
    @PrimaryKeyJoinColumn
    private Student student;
    @OneToOne(mappedBy = "user" , orphanRemoval = false)
    @PrimaryKeyJoinColumn
    private Teacher teacher;
    @OneToMany(mappedBy = "user", orphanRemoval = false)
    private Set<Comment> comments;
}

