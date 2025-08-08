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

import com.nearstar.sftpmanager.model.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long>
{

    // Find by site name (since you store siteName as String)
    List<TransactionLog> findBySiteName( String siteName );

    // Find by username (since you store username as String)
    List<TransactionLog> findByUsername( String username );

    // Find by action type
    List<TransactionLog> findByAction( String action );

    // Find failed actions (assuming "ERROR" or "FAILED" in action or details)
    @Query("SELECT t FROM TransactionLog t WHERE t.action LIKE '%ERROR%' OR t.action LIKE '%FAILED%' OR t.details LIKE '%ERROR%' OR t.details LIKE '%FAILED%'")
    List<TransactionLog> findFailedTransactions();

    // Find successful actions (assuming "SUCCESS" in action or details)
    @Query("SELECT t FROM TransactionLog t WHERE t.action LIKE '%SUCCESS%' OR t.details LIKE '%SUCCESS%'")
    List<TransactionLog> findSuccessfulTransactions();

    // Date range query (already correct)
    @Query("SELECT t FROM TransactionLog t WHERE t.timestamp BETWEEN :startDate AND :endDate")
    List<TransactionLog> findByDateRange( @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate );

    // Find by site and action (updated to use siteName)
    @Query("SELECT t FROM TransactionLog t WHERE t.siteName = :siteName AND t.action = :action")
    List<TransactionLog> findBySiteAndAction( @Param("siteName") String siteName,
                                              @Param("action") String action );

    // Find recent transactions (last 24 hours)
    @Query("SELECT t FROM TransactionLog t WHERE t.timestamp >= :since ORDER BY t.timestamp DESC")
    List<TransactionLog> findRecentTransactions( @Param("since") LocalDateTime since );

    // Default method for recent transactions
    default List<TransactionLog> findRecentTransactions()
    {
        return findRecentTransactions( LocalDateTime.now().minusDays( 1 ) );
    }

    // Find by username and date range
    @Query("SELECT t FROM TransactionLog t WHERE t.username = :username AND t.timestamp BETWEEN :startDate AND :endDate")
    List<TransactionLog> findByUsernameAndDateRange( @Param("username") String username,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate );
}