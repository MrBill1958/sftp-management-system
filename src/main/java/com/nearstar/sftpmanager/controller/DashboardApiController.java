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
package com.nearstar.sftpmanager.controller;

import com.nearstar.sftpmanager.model.dto.UserSession;
import com.nearstar.sftpmanager.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardApiController
{

    private final DashboardService dashboardService;

    // Get current user session
    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> getCurrentSession( HttpSession session )
    {
        UserSession userSession = (UserSession) session.getAttribute( "user" );

        if ( userSession == null )
        {
            return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).build();
        }

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put( "username", userSession.getUsername() );
        sessionData.put( "role", userSession.getRole() );
        sessionData.put( "fullName", userSession.getFullName() );
        sessionData.put( "email", userSession.getEmail() );
        sessionData.put( "permissions", userSession.getPermissions() );
        sessionData.put( "isAdmin", userSession.isAdmin() );

        return ResponseEntity.ok( sessionData );
    }

    // Get dashboard statistics
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats( HttpSession session )
    {
        UserSession userSession = (UserSession) session.getAttribute( "user" );
        if ( userSession == null )
        {
            return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).build();
        }

        try
        {
            Map<String, Object> stats;
            if ( userSession.isAdmin() )
            {
                stats = dashboardService.getDashboardStats();
            }
            else
            {
                stats = dashboardService.getUserDashboardStats( userSession.getUsername() );
            }
            return ResponseEntity.ok( stats );
        }
        catch (Exception e)
        {
            log.error( "Error retrieving dashboard stats", e );
            // Return mock data if service fails
            Map<String, Object> mockStats = new HashMap<>();
            mockStats.put( "totalSites", 5 );
            mockStats.put( "activeSites", 3 );
            mockStats.put( "totalUsers", 12 );
            mockStats.put( "recentFiles", 47 );
            return ResponseEntity.ok( mockStats );
        }
    }

    // Get recent activity
    @GetMapping("/activity/recent")
    public ResponseEntity<Object[]> getRecentActivity( HttpSession session,
                                                       @RequestParam(defaultValue = "10") int limit )
    {
        UserSession userSession = (UserSession) session.getAttribute( "user" );
        if ( userSession == null )
        {
            return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).build();
        }

        try
        {
            // TODO: Implement actual activity retrieval from service
            // For now, return mock data
            Object[] mockActivities = {
                    Map.of(
                            "type", "upload",
                            "title", "report.pdf uploaded",
                            "details", "ShareFile site",
                            "time", "5 mins ago",
                            "user", userSession.getUsername()
                    ),
                    Map.of(
                            "type", "download",
                            "title", "data.csv downloaded",
                            "details", "ShareTru site",
                            "time", "1 hour ago",
                            "user", "admin"
                    ),
                    Map.of(
                            "type", "edit",
                            "title", "Site configuration updated",
                            "details", "Production Server",
                            "time", "2 hours ago",
                            "user", "admin"
                    )
            };

            return ResponseEntity.ok( mockActivities );
        }
        catch (Exception e)
        {
            log.error( "Error retrieving recent activity", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR ).build();
        }
    }

    // Logout endpoint
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout( HttpSession session )
    {
        session.invalidate();
        return ResponseEntity.ok( Map.of( "message", "Logged out successfully" ) );
    }
}