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

import com.nearstar.sftpmanager.model.dto.UserSession;
import com.nearstar.sftpmanager.repository.SiteRepository;
import com.nearstar.sftpmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService
{

    private final SiteRepository siteRepository;
    private final UserRepository userRepository;

    public Map<String, Object> getDashboardStats()
    {
        Map<String, Object> stats = new HashMap<>();

        stats.put( "totalSites", siteRepository.count() );
        stats.put( "totalUsers", userRepository.count() );
        stats.put( "activeSites", siteRepository.count() ); // Placeholder - you can improve this

        return stats;
    }

    public Map<String, Object> getUserDashboardStats( String username )
    {
        Map<String, Object> stats = new HashMap<>();

        stats.put( "userSites", siteRepository.findUserSites( username ).size() );
        stats.put( "userActiveSites", siteRepository.findUserSites( username ).size() ); // Placeholder

        return stats;
    }

    // ADD THIS METHOD that was being called in DashboardController
    public Map<String, Object> getStats( UserSession userSession )
    {
        if ( userSession.isAdmin() )
        {
            return getDashboardStats();
        }
        else
        {
            return getUserDashboardStats( userSession.getUsername() );
        }
    }
}
