package com.nearstar.sftpmanager.model.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.Set;

@Data
public class UserDTO {
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String firstName;

    private String lastName;

    private Set<String> roles;

    private String accessGroup;

    private boolean enabled = true;
}