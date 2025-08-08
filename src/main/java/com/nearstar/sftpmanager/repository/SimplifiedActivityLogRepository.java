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
package com.nearstar.sftpmanager.repository;

import com.nearstar.sftpmanager.model.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SimplifiedActivityLogRepository extends JpaRepository<ActivityLog, Long>
{

    // Native query to get recent activities without complex JPA
    @Query(value = "SELECT * FROM activity_logs ORDER BY created_at DESC LIMIT :limit",
            nativeQuery = true)
    List<ActivityLog> findRecentActivities( @Param("limit") int limit );

    // Native query to get user activities
    @Query(value = "SELECT * FROM activity_logs WHERE username = :username " +
            "ORDER BY created_at DESC LIMIT :limit",
            nativeQuery = true)
    List<ActivityLog> findUserActivities( @Param("username") String username,
                                          @Param("limit") int limit );

    // Native query to count recent files
    @Query(value = "SELECT COUNT(*) FROM activity_logs " +
            "WHERE username = :username " +
            "AND type IN ('UPLOAD', 'DOWNLOAD', 'EDIT') " +
            "AND created_at > DATE_SUB(NOW(), INTERVAL :days DAY)",
            nativeQuery = true)
    int countRecentFiles( @Param("username") String username, @Param("days") int days );

    // Native query for dashboard stats
    @Query(value = "SELECT " +
            "COUNT(CASE WHEN type = 'LOGIN' THEN 1 END) as logins, " +
            "COUNT(CASE WHEN type = 'UPLOAD' THEN 1 END) as uploads, " +
            "COUNT(CASE WHEN type = 'DOWNLOAD' THEN 1 END) as downloads " +
            "FROM activity_logs " +
            "WHERE created_at > DATE_SUB(NOW(), INTERVAL 7 DAY)",
            nativeQuery = true)
    Map<String, Object> getDashboardStats();
}