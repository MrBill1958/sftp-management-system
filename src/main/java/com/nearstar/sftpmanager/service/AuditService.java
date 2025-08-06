package com.nearstar.sftpmanager.service;

import com.nearstar.sftpmanager.model.dto.AuditReportRequest;
import com.nearstar.sftpmanager.model.entity.AuditLog;
import com.nearstar.sftpmanager.model.entity.User;
import com.nearstar.sftpmanager.model.enums.ActionType;
import com.nearstar.sftpmanager.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public void logAction(ActionType actionType, String entityType, Long entityId,
                          String oldValue, String newValue, String description) {
        try {
            AuditLog auditLog = new AuditLog();

            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                auditLog.setUser(user);
                auditLog.setUsername(user.getUsername());
            } else {
                auditLog.setUsername("SYSTEM");
            }

            // Get request info
            HttpServletRequest request = getCurrentHttpRequest();
            if (request != null) {
                auditLog.setIpAddress(getClientIp(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            } else {
                auditLog.setIpAddress("SYSTEM");
            }

            auditLog.setActionType(actionType);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setOldValue(oldValue);
            auditLog.setNewValue(newValue);
            auditLog.setDescription(description);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setSuccessful(true);

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            log.error("Failed to create audit log entry", e);
        }
    }

    public void logFailedAction(ActionType actionType, String entityType, Long entityId,
                                String description, String errorMessage) {
        try {
            AuditLog auditLog = new AuditLog();

            // Set user and request info (same as above)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                auditLog.setUser(user);
                auditLog.setUsername(user.getUsername());
            } else {
                auditLog.setUsername("SYSTEM");
            }

            HttpServletRequest request = getCurrentHttpRequest();
            if (request != null) {
                auditLog.setIpAddress(getClientIp(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            } else {
                auditLog.setIpAddress("SYSTEM");
            }

            auditLog.setActionType(actionType);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setDescription(description);
            auditLog.setSuccessful(false);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setTimestamp(LocalDateTime.now());

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            log.error("Failed to create failed audit log entry", e);
        }
    }

    public List<AuditLog> getAuditLogs(AuditReportRequest request) {
        // Implement query based on request parameters
        if (request.getStartDate() != null && request.getEndDate() != null) {
            return auditLogRepository.findByDateRange(request.getStartDate(), request.getEndDate());
        }
        return auditLogRepository.findAll();
    }

    public byte[] exportAuditReport(AuditReportRequest request) throws Exception {
        List<AuditLog> logs = getAuditLogs(request);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(baos)) {
            // Write CSV header
            writer.println("Timestamp,Username,IP Address,Action Type,Entity Type,Entity ID," +
                    "Description,Success,Error Message");

            // Write data
            for (AuditLog log : logs) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        log.getTimestamp().format(formatter),
                        log.getUsername(),
                        log.getIpAddress(),
                        log.getActionType(),
                        log.getEntityType(),
                        log.getEntityId() != null ? log.getEntityId() : "",
                        escapeCSV(log.getDescription()),
                        log.isSuccessful(),
                        escapeCSV(log.getErrorMessage())
                );
            }
        }

        return baos.toByteArray();
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0].trim();
        }
        String xClientIp = request.getHeader("X-Real-IP");
        if (xClientIp != null) {
            return xClientIp;
        }
        return request.getRemoteAddr();
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}