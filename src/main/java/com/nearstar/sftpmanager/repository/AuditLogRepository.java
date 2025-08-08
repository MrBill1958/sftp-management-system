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

import com.nearstar.sftpmanager.model.entity.AuditLog;
import com.nearstar.sftpmanager.model.enums.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>
{
    List<AuditLog> findByUserId( Long userId );

    List<AuditLog> findByActionType( ActionType actionType );

    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate")
    List<AuditLog> findByDateRange( @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate );

    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId")
    List<AuditLog> findByEntity( @Param("entityType") String entityType,
                                 @Param("entityId") Long entityId );

    List<AuditLog> findBySuccessfulFalse();
}
