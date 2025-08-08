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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Slf4j
// @RestController  // COMMENTED OUT TO AVOID CONFLICT
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController
{

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats( HttpSession session )
    {
        try
        {
            // Get user from session
            UserSession userSession = (UserSession) session.getAttribute( "user" );
            if ( userSession == null )
            {
                return ResponseEntity.status( 401 ).build();
            }

            // Get dashboard stats based on user role
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
            return ResponseEntity.status( 500 ).build();
        }
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<?> getRecentActivity(
            HttpSession session,
            @RequestParam(defaultValue = "10") int limit )
    {
        try
        {
            // Get user from session
            UserSession userSession = (UserSession) session.getAttribute( "user" );
            if ( userSession == null )
            {
                return ResponseEntity.status( 401 ).build();
            }

            // Implementation for recent activity
            return ResponseEntity.ok( Map.of( "activities", "Not implemented yet" ) );
        }
        catch (Exception e)
        {
            log.error( "Error retrieving recent activity", e );
            return ResponseEntity.status( 500 ).build();
        }
    }
}