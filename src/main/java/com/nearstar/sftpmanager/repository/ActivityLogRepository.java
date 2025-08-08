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
import com.nearstar.sftpmanager.model.enums.ActivityType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long>
{

    // Find by username with pagination
    List<ActivityLog> findByUsername( String username, Pageable pageable );

    // Find by type with pagination
    List<ActivityLog> findByType( ActivityType type, Pageable pageable );

    // Find by site name
    List<ActivityLog> findBySiteName( String siteName );

    // Find recent activities - FIXED: Use 'timestamp' instead of 'createdAt'
    @Query("SELECT a FROM ActivityLog a WHERE a.timestamp > :date ORDER BY a.timestamp DESC")
    List<ActivityLog> findRecentActivities( @Param("date") LocalDateTime date );

    // Count recent files - FIXED: Use 'timestamp' instead of 'createdAt'
    @Query("SELECT COUNT(a) FROM ActivityLog a WHERE a.username = :username " +
            "AND a.type IN (:types) " +
            "AND a.timestamp > :date")
    int countRecentFiles( @Param("username") String username,
                          @Param("types") List<ActivityType> types,
                          @Param("date") LocalDateTime date );

    // Default method for counting recent files (using enum types instead of strings)
    default int countRecentFiles( String username, int days )
    {
        List<ActivityType> fileTypes = List.of(
                ActivityType.UPLOAD,
                ActivityType.DOWNLOAD,
                ActivityType.EDIT
        );
        return countRecentFiles( username, fileTypes, LocalDateTime.now().minusDays( days ) );
    }

    // Find activities by date range
    @Query("SELECT a FROM ActivityLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    List<ActivityLog> findByDateRange( @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate );

    // Find activities by username and date range
    @Query("SELECT a FROM ActivityLog a WHERE a.username = :username " +
            "AND a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    List<ActivityLog> findByUsernameAndDateRange( @Param("username") String username,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate );

    // Count activities by type for a user
    @Query("SELECT COUNT(a) FROM ActivityLog a WHERE a.username = :username AND a.type = :type")
    long countByUsernameAndType( @Param("username") String username, @Param("type") ActivityType type );

    // Find latest activities (limited)
    @Query("SELECT a FROM ActivityLog a ORDER BY a.timestamp DESC")
    List<ActivityLog> findLatestActivities( Pageable pageable );

    // Default method to get recent activities (last 7 days)
    default List<ActivityLog> findRecentActivities()
    {
        return findRecentActivities( LocalDateTime.now().minusDays( 7 ) );
    }
}