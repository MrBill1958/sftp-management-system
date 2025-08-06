package com.nearstar.sftpmanager.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditReportRequest {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String username;
    private String actionType;
    private String entityType;
    private boolean includeSystemActions;
}