package com.uni.ethesis.web.view.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserViewModel {
    @NotBlank
    @Size(min = 5, max = 20, message="Min 5, Max 20")
    private String firstName;
    @NotBlank
    @Size(min = 5, max = 20, message="Min 5, Max 20")
    private String lastName;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String role;
}
