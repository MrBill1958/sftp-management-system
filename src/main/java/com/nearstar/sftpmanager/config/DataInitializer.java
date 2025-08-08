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
package com.nearstar.sftpmanager.config;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.nearstar.sftpmanager.model.entity.User;
import com.nearstar.sftpmanager.model.enums.UserRole;
import com.nearstar.sftpmanager.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer
{

    @Autowired
    private UserRepository userRepository;

    // Use standalone BCrypt instead of Spring Security's BCryptPasswordEncoder
    private final BCrypt.Hasher bcrypt = BCrypt.withDefaults();

    @PostConstruct
    public void initializeData()
    {
        createDefaultUsers();
    }

    private void createDefaultUsers()
    {
        try
        {
            // Create admin user if doesn't exist
            if ( !userRepository.existsByUsername( "admin" ) )
            {
                User admin = new User();
                admin.setUsername( "admin" );
                admin.setPassword( bcrypt.hashToString( 12, "admin123".toCharArray() ) );
                admin.setEmail( "admin@nearstar.com.com" );
                admin.setFullName( "System Administrator" );
                admin.setRole( UserRole.ADMIN );
                admin.setActive( true );
                admin.setLocked( false );
                admin.setFailedAttempts( 0 );
                admin.setCreatedAt( LocalDateTime.now() );

                // Set required database fields
                admin.setAccountNonExpired( true );
                admin.setAccountNonLocked( true );
                admin.setCredentialsNonExpired( true );
                admin.setEnabled( true );
                admin.setMustChangePassword( false );

                userRepository.save( admin );
                System.out.println( "Created admin user: admin / admin123" );
            }

            // Create regular user if doesn't exist
            if ( !userRepository.existsByUsername( "user" ) )
            {
                User user = new User();
                user.setUsername( "user" );
                user.setPassword( bcrypt.hashToString( 12, "user123".toCharArray() ) );
                user.setEmail( "user@nearstar.com.com" );
                user.setFullName( "Regular User" );
                user.setRole( UserRole.USER );
                user.setActive( true );
                user.setLocked( false );
                user.setFailedAttempts( 0 );
                user.setCreatedAt( LocalDateTime.now() );

                // Set required database fields
                user.setAccountNonExpired( true );
                user.setAccountNonLocked( true );
                user.setCredentialsNonExpired( true );
                user.setEnabled( true );
                user.setMustChangePassword( false );

                userRepository.save( user );
                System.out.println( "Created regular user: user / user123" );
            }

            // Create test viewer if doesn't exist
            if ( !userRepository.existsByUsername( "viewer" ) )
            {
                User viewer = new User();
                viewer.setUsername( "viewer" );
                viewer.setPassword( bcrypt.hashToString( 12, "viewer123".toCharArray() ) );
                viewer.setEmail( "viewer@nearstar.com" );
                viewer.setFullName( "Test Viewer" );
                viewer.setRole( UserRole.VIEWER );
                viewer.setActive( true );
                viewer.setLocked( false );
                viewer.setFailedAttempts( 0 );
                viewer.setCreatedAt( LocalDateTime.now() );

                // Set required database fields
                viewer.setAccountNonExpired( true );
                viewer.setAccountNonLocked( true );
                viewer.setCredentialsNonExpired( true );
                viewer.setEnabled( true );
                viewer.setMustChangePassword( false );

                userRepository.save( viewer );
                System.out.println( "Created viewer user: viewer / viewer123" );
            }

        }
        catch (Exception e)
        {
            System.err.println( "Error creating default users: " + e.getMessage() );
            e.printStackTrace(); // Show full stack trace for debugging
        }
    }
}