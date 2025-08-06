package com.nearstar.sftpmanager.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class FileTransferRequest {
    @NotNull(message = "Site ID is required")
    private Long siteId;

    @NotBlank(message = "Remote path is required")
    private String remotePath;

    private boolean verifyTransfer = true;

    private boolean createDirectories = false;

    private String localPath;
}