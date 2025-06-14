package com.uni.ethesis.data.dto;

import java.util.UUID;

public class StudentDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String universityId; // Changed from facultyNumber

    // Constructors
    public StudentDto() {
    }

    public StudentDto(UUID id, String firstName, String lastName, String universityId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.universityId = universityId; // Changed from facultyNumber
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

    public String getUniversityId() { // Changed from getFacultyNumber
        return universityId;
    }

    public void setUniversityId(String universityId) { // Changed from setFacultyNumber
        this.universityId = universityId;
    }

    // toString, equals, hashCode (optional but good practice)
}
