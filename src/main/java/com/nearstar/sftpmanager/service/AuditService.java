/**
 * NearStar, Inc.
 * 410 E. Main Street
 * Lewisville, Texas  76057
 * Tel: 1.972.221.4068
 * <p>
 * Copyright � 2025 NearStar Incorporated. All rights reserved.
 * <p>
 * <p>
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF NEARSTAR Inc.
 * <p>
 * THIS COPYRIGHT NOTICE DOES NOT EVIDENCE ANY
 * ACTUAL OR INTENDED PUBLICATION OF SUCH SOURCE CODE.
 * This software and its source code are proprietary and confidential to NearStar Incorporated.
 * Unauthorized copying, modification, distribution, or use of this software, in whole or in part,
 * is strictly prohibited without the prior written consent of the copyright holder.
 * Portions of this software may utilize or be derived from open-source software
 * and publicly available frameworks licensed under their respective licenses.
 * <p>
 * This code may also include contributions developed with the assistance of AI-based tools.
 * All open-source dependencies are used in accordance with their applicable licenses,
 * and full attribution is maintained in the corresponding documentation (e.g., NOTICE or LICENSE files).
 * For inquiries regarding licensing or usage, please make request by going to nearstar.com.
 *
 * @file ${NAME}.java
 * @author ${USER} <${USER}@nearstar.com>
 * @version 1.0.0
 * @date ${DATE}
 * @project SFTP Site Management System
 * @package com.nearstar.sftpmanager
 * <p>
 * Copyright    ${YEAR} Nearstar
 * @license Proprietary
 * @modified
 */
package com.nearstar.sftpmanager.service;

import com.nearstar.sftpmanager.model.dto.AuditReportRequest;
import com.nearstar.sftpmanager.model.entity.AuditLog;
import com.nearstar.sftpmanager.model.enums.ActionType;
import com.nearstar.sftpmanager.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class AuditService
{

    private final AuditLogRepository auditLogRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );

    @Transactional
    public void logAction( ActionType actionType, String entityType, Long entityId,
                           String oldValue, String newValue, String description, String username )
    {
        try
        {
            AuditLog auditLog = new AuditLog();

            // Use provided username instead of Spring Security
            auditLog.setUsername( username != null ? username : "SYSTEM" );

            // Get request info
            HttpServletRequest request = getCurrentHttpRequest();
            if ( request != null )
            {
                auditLog.setIpAddress( getClientIp( request ) );
                auditLog.setUserAgent( request.getHeader( "User-Agent" ) );
            }
            else
            {
                auditLog.setIpAddress( "SYSTEM" );
            }

            auditLog.setActionType( actionType );
            auditLog.setEntityType( entityType );
            auditLog.setEntityId( entityId );
            auditLog.setOldValue( oldValue );
            auditLog.setNewValue( newValue );
            auditLog.setDescription( description );
            auditLog.setTimestamp( LocalDateTime.now() );
            auditLog.setSuccessful( true );

            auditLogRepository.save( auditLog );
        }
        catch (Exception e)
        {
            log.error( "Failed to create audit log entry", e );
        }
    }

    @Transactional
    public void logFailedAction( ActionType actionType, String entityType, Long entityId,
                                 String description, String errorMessage, String username )
    {
        try
        {
            AuditLog auditLog = new AuditLog();

            // Use provided username instead of Spring Security
            auditLog.setUsername( username != null ? username : "SYSTEM" );

            // Get request info
            HttpServletRequest request = getCurrentHttpRequest();
            if ( request != null )
            {
                auditLog.setIpAddress( getClientIp( request ) );
                auditLog.setUserAgent( request.getHeader( "User-Agent" ) );
            }
            else
            {
                auditLog.setIpAddress( "SYSTEM" );
            }

            auditLog.setActionType( actionType );
            auditLog.setEntityType( entityType );
            auditLog.setEntityId( entityId );
            auditLog.setDescription( description );
            auditLog.setTimestamp( LocalDateTime.now() );
            auditLog.setSuccessful( false );
            auditLog.setErrorMessage( errorMessage );

            auditLogRepository.save( auditLog );
        }
        catch (Exception e)
        {
            log.error( "Failed to create failed audit log entry", e );
        }
    }

    public List<AuditLog> getAuditLogs( AuditReportRequest request )
    {
        // Implement query based on request parameters
        if ( request.getStartDate() != null && request.getEndDate() != null )
        {
            return auditLogRepository.findByDateRange( request.getStartDate(), request.getEndDate() );
        }
        return auditLogRepository.findAll();
    }

    public byte[] exportAuditReport( AuditReportRequest request ) throws Exception
    {
        List<AuditLog> logs = getAuditLogs( request );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try ( PrintWriter writer = new PrintWriter( baos ) )
        {
            // Write CSV header
            writer.println( "Timestamp,Username,IP Address,Action Type,Entity Type,Entity ID," +
                    "Description,Success,Error Message" );

            // Write data
            for (AuditLog log : logs)
            {
                writer.printf( "%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        log.getTimestamp().format( formatter ),
                        log.getUsername(),
                        log.getIpAddress(),
                        log.getActionType(),
                        log.getEntityType(),
                        log.getEntityId() != null ? log.getEntityId() : "",
                        escapeCSV( log.getDescription() ),
                        log.isSuccessful(),
                        escapeCSV( log.getErrorMessage() )
                );
            }
        }

        return baos.toByteArray();
    }

    private HttpServletRequest getCurrentHttpRequest()
    {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIp( HttpServletRequest request )
    {
        String xfHeader = request.getHeader( "X-Forwarded-For" );
        if ( xfHeader != null )
        {
            return xfHeader.split( "," )[0].trim();
        }

        String xClientIp = request.getHeader( "X-Real-IP" );
        if ( xClientIp != null )
        {
            return xClientIp;
        }

        return request.getRemoteAddr();
    }

    private String escapeCSV( String value )
    {
        if ( value == null )
        {
            return "";
        }
        if ( value.contains( "," ) || value.contains( "\"" ) || value.contains( "\n" ) )
        {
            return "\"" + value.replace( "\"", "\"\"" ) + "\"";
        }
        return value;
    }
}