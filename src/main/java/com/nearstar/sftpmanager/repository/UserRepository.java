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

import com.nearstar.sftpmanager.model.entity.User;
import com.nearstar.sftpmanager.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>
{
    // Find user by username
    Optional<User> findByUsername( String username );

    // Find user by email
    Optional<User> findByEmail( String email );

    // Check if username exists
    boolean existsByUsername( String username );

    // Check if email exists
    boolean existsByEmail( String email );

    // Update last login time
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateLastLogin( @Param("userId") Long userId, @Param("lastLogin") LocalDateTime lastLogin );

    // Count online users (logged in within last 30 minutes)
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin > :time")
    int countOnlineUsers( @Param("time") LocalDateTime time );

    // Default method for counting online users
    default int countOnlineUsers()
    {
        return countOnlineUsers( LocalDateTime.now().minusMinutes( 30 ) );
    }

    // Find users by role
    List<User> findByRole( UserRole role );

    // Find active users
    List<User> findByActiveTrue();

    // Find locked users
    List<User> findByLockedTrue();

    // Reset failed login attempts
    @Query("UPDATE User u SET u.failedAttempts = 0 WHERE u.username = :username")
    void resetFailedAttempts( @Param("username") String username );

    // Increment failed login attempts
    @Query("UPDATE User u SET u.failedAttempts = u.failedAttempts + 1 WHERE u.username = :username")
    void incrementFailedAttempts( @Param("username") String username );

    // Lock user account
    @Query("UPDATE User u SET u.locked = true WHERE u.username = :username")
    void lockUser( @Param("username") String username );

    // Unlock user account
    @Query("UPDATE User u SET u.locked = false, u.failedAttempts = 0 WHERE u.username = :username")
    void unlockUser( @Param("username") String username );

    // Count users by role
    long countByRole( UserRole role );
}
