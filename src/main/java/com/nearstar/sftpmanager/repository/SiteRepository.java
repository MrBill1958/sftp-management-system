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

import com.nearstar.sftpmanager.model.entity.Site;
import com.nearstar.sftpmanager.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long>
{

    // Find site by name
    Optional<Site> findBySiteName( String siteName );

    // Check if site name exists
    boolean existsBySiteName( String siteName );

    // Find sites by owner
    List<Site> findByOwnerId( Long ownerId );

    // Find sites by status - FIXED: Use status instead of active
    List<Site> findByStatus( Status status );

    // Count active sites - FIXED: Use status instead of active
    @Query("SELECT COUNT(s) FROM Site s WHERE s.status = 'ACTIVE'")
    long countActiveSites();

    // Alternative using method name convention (recommended)
    long countByStatus( Status status );

    // Find active sites
    default List<Site> findActiveSites()
    {
        return findByStatus( Status.ACTIVE );
    }

    // Count active sites using the alternative method
    default long countActiveSitesAlternative()
    {
        return countByStatus( Status.ACTIVE );
    }

    // Find sites by username (assuming you're looking for sites owned by a user)
    @Query("SELECT s FROM Site s WHERE s.owner.username = :username")
    List<Site> findUserSites( @Param("username") String username );

    // Alternative: Find sites by owner username
    List<Site> findByOwnerUsername( String username );
}