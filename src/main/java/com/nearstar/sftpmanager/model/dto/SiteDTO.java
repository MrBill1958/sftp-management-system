package com.nearstar.sftpmanager.model.dto;

import com.nearstar.sftpmanager.model.enums.Status;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class SiteDTO {
    private Long id;

    @NotBlank(message = "Site name is required")
    private String siteName;

    @NotBlank(message = "IP address is required")
    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$|^[a-zA-Z0-9.-]+$",
            message = "Invalid IP address or hostname")
    private String ipAddress;

    @Min(value = 1, message = "Port must be between 1 and 65535")
    @Max(value = 65535, message = "Port must be between 1 and 65535")
    private int port = 22;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    private String targetPath = "/";

    @Email(message = "Invalid email format")
    private String emailNotification;

    private Status status = Status.ACTIVE;

    private String description;

    private Long ownerId;

    private String sshKey;

    private boolean saveKnownHost = false;
}