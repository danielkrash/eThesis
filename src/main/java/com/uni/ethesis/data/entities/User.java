package com.uni.ethesis.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
@Entity
public class User extends BaseEntity {
    @Column(name = "first_name" , columnDefinition = "text")
    private String firstName;
    @Column(name = "last_name" , columnDefinition = "text")
    private String lastName;
    @Column(unique = true, name = "email" , columnDefinition = "text")
    @Email(message = "Email should be valid")
    private String email;
    @OneToMany(mappedBy = "user", orphanRemoval = false)
    private Set<DepartmentAppointment> appointments;
    @OneToMany(mappedBy = "user", orphanRemoval = false)
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
