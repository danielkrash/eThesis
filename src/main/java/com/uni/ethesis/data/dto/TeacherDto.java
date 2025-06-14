package com.uni.ethesis.data.dto;

import java.util.UUID;

import com.uni.ethesis.enums.TeacherPosition;

public class TeacherDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email; // From associated User entity
    private TeacherPosition position;

    // Constructors
    public TeacherDto() {
    }

    public TeacherDto(UUID id, String firstName, String lastName, String email, TeacherPosition position) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.position = position;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TeacherPosition getPosition() {
        return position;
    }

    public void setPosition(TeacherPosition position) {
        this.position = position;
    }
}
