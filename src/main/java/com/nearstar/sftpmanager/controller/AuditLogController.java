/**
 * Copyright © 2025 NearStar Incorporated. All rights reserved.
 *
 * This software and its source code are proprietary and confidential
 * to NearStar Incorporated. Unauthorized copying, modification,
 * distribution, or use of this software, in whole or in part,
 * is strictly prohibited without the prior written consent of the copyright holder.
 *
 * Portions of this software may utilize or be derived from open-source software
 * and publicly available frameworks licensed under their respective licenses.
 *
 * This code may also include contributions developed with the assistance of AI-based tools.
 *
 * All open-source dependencies are used in accordance with their applicable licenses,
 * and full attribution is maintained in the corresponding documentation (e.g., NOTICE or LICENSE files).
 *
 * For inquiries regarding licensing or usage, please contact: bill.sanders@nearstar.com
 *
 * @file        UserService.java
 * @author      Bill Sanders <bill.sanders@nearstar.com>
 * @version     1.0.0
 * @date        2025-08-03
 * @brief       Brief description of the file's purpose
 *
 * @copyright   Copyright (c) 2025 NearStar Incorporated
 * @license     MIT License
 *
 * @modified    2025-08-06 - Bill Sanders - Initialized in Git
 *
 * @todo
 * @bug
 * @deprecated
 */
package com.nearstar.sftpmanager.controller;

import com.nearstar.sftpmanager.model.dto.AuditReportRequest;
import com.nearstar.sftpmanager.model.entity.AuditLog;
import com.nearstar.sftpmanager.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String entityType,
            @RequestParam(defaultValue = "false") boolean includeSystemActions) {

        AuditReportRequest request = new AuditReportRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setUsername(username);
        request.setActionType(actionType);
        request.setEntityType(entityType);
        request.setIncludeSystemActions(includeSystemActions);

        List<AuditLog> logs = auditService.getAuditLogs(request);

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String actionType,
            @RequestParam(defaultValue = "csv") String format) {

        try {
            AuditReportRequest request = new AuditReportRequest();
            request.setStartDate(startDate);
            request.setEndDate(endDate);
            request.setUsername(username);
            request.setActionType(actionType);

            byte[] reportData = auditService.exportAuditReport(request);

            String filename = String.format("audit-logs-%s.%s",
                    LocalDateTime.now().toString().replace(":", "-"), format);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(reportData);

        } catch (Exception e) {
            log.error("Error exporting audit logs", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getAuditSummary(
            @RequestParam(defaultValue = "7") int days) {

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        AuditReportRequest request = new AuditReportRequest();
        request.setStartDate(startDate);
        request.setEndDate(LocalDateTime.now());

        List<AuditLog> logs = auditService.getAuditLogs(request);

        // Calculate summary statistics
        long totalActions = logs.size();
        long failedActions = logs.stream().filter(log -> !log.isSuccessful()).count();
        long uniqueUsers = logs.stream().map(AuditLog::getUsername).distinct().count();

        return ResponseEntity.ok( Map.of(
                "totalActions", totalActions,
                "failedActions", failedActions,
                "successRate", totalActions > 0 ? ((totalActions - failedActions) * 100.0 / totalActions) : 100.0,
                "uniqueUsers", uniqueUsers,
                "period", days + " days"
        ));
    }
}