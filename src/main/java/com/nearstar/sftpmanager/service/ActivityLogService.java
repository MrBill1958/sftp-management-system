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

import com.nearstar.sftpmanager.model.entity.ActivityLog;
import com.nearstar.sftpmanager.model.enums.ActivityType;
import com.nearstar.sftpmanager.repository.SimplifiedActivityLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ActivityLogService
{

    @Autowired
    private SimplifiedActivityLogRepository activityLogRepository;

    public void logActivity( String username, ActivityType type, String title, String details, String siteName )
    {
        ActivityLog log = new ActivityLog();
        log.setUsername( username );
        log.setType( type );
        log.setTitle( title );
        log.setDetails( details );
        log.setSiteName( siteName );
        activityLogRepository.save( log );
    }

    public void logLogin( String username )
    {
        logActivity( username, ActivityType.LOGIN, "User logged in", null, null );
    }

    public void logLogout( String username )
    {
        logActivity( username, ActivityType.LOGOUT, "User logged out", null, null );
    }

    public void logFileUpload( String username, String fileName, String siteName )
    {
        logActivity( username, ActivityType.UPLOAD, fileName + " uploaded", siteName + " site", siteName );
    }

    public void logFileDownload( String username, String fileName, String siteName )
    {
        logActivity( username, ActivityType.DOWNLOAD, fileName + " downloaded", siteName + " site", siteName );
    }

    public void logFileDelete( String username, String fileName, String siteName )
    {
        logActivity( username, ActivityType.DELETE, fileName + " deleted", siteName + " site", siteName );
    }

    public void logSiteEdit( String username, String siteName )
    {
        logActivity( username, ActivityType.SFTP_CONNECTION_UPDATED, "Site configuration updated", siteName, siteName );
    }

    public void logSiteCreate( String username, String siteName )
    {
        logActivity( username, ActivityType.SFTP_CONNECTION_CREATED, "Site created", siteName, siteName );
    }

    public void logSiteDelete( String username, String siteName )
    {
        logActivity( username, ActivityType.SFTP_CONNECTION_DELETED, "Site deleted", siteName, siteName );
    }

    public void logUserCreate( String adminUsername, String newUsername )
    {
        logActivity( adminUsername, ActivityType.CREATE, "User created: " + newUsername, "New user account", null );
    }

    public void logUserUpdate( String adminUsername, String updatedUsername )
    {
        logActivity( adminUsername, ActivityType.USER_UPDATED, "User updated: " + updatedUsername, "User account modified", null );
    }

    public void logUserDelete( String adminUsername, String deletedUsername )
    {
        logActivity( adminUsername, ActivityType.USER_DELETED, "User deleted: " + deletedUsername, "User account removed", null );
    }

    public void logConnectionTest( String username, String siteName, boolean success )
    {
        String title = success ? "Connection test successful" : "Connection test failed";
        String details = success ? "Site connection verified" : "Site connection failed";
        logActivity( username, ActivityType.SFTP_CONNECTION_CREATED, title, details + " - " + siteName, siteName );
    }

    public void logGenericEdit( String username, String itemName, String details )
    {
        logActivity( username, ActivityType.EDIT, itemName + " edited", details, null );
    }

    public List<ActivityLog> getRecentActivities( int limit )
    {
        return activityLogRepository.findRecentActivities( limit );
    }

    public List<ActivityLog> getUserActivities( String username, int limit )
    {
        return activityLogRepository.findUserActivities( username, limit );
    }

    public List<ActivityLog> getSiteActivities( String siteName, int limit )
    {
        // You might want to add this method to your repository
        return activityLogRepository.findRecentActivities( limit ); // Placeholder
    }

    public void logActivityWithException( String username, ActivityType type, String title, Exception e )
    {
        String details = "Error: " + e.getMessage();
        logActivity( username, type, title + " (Failed)", details, null );
    }
}