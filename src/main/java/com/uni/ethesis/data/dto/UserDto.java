package com.uni.ethesis.data.dto;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import lombok.Data;

/**
 * DTO for {@link com.uni.ethesis.data.entities.User}
 */
@Data
public class UserDto implements Serializable {
    UUID id;
    OffsetDateTime createdAt;
    OffsetDateTime lastModifiedAt;
    String firstName;
    String lastName;
    @Email(message = "Email should be valid")
    String email;
}