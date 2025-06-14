package com.uni.ethesis.data.entities;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
    @OneToMany(mappedBy = "user", orphanRemoval = false , fetch = FetchType.LAZY , cascade = CascadeType.ALL)
    private Set<DepartmentAppointment> appointments;
    @OneToMany(mappedBy = "user", orphanRemoval = false , fetch = FetchType.LAZY , cascade = CascadeType.ALL)
    private Set<UserInDepartment> departments;
    @OneToMany(mappedBy = "user", orphanRemoval = false , cascade = CascadeType.ALL)
    private Set<Comment> comments;
}

