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

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.nearstar.sftpmanager.model.dto.UserDTO;
import com.nearstar.sftpmanager.model.dto.UserSession;
import com.nearstar.sftpmanager.model.entity.User;
import com.nearstar.sftpmanager.model.enums.UserRole;
import com.nearstar.sftpmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService
{

    @Autowired
    private UserRepository userRepository;

    // Use standalone BCrypt instead of Spring Security's BCryptPasswordEncoder
    private final BCrypt.Hasher bcrypt = BCrypt.withDefaults();

    public User authenticate( String username, String password )
    {
        Optional<User> userOpt = userRepository.findByUsername( username );
        if ( userOpt.isEmpty() )
        {
            return null;
        }

        User user = userOpt.get();

        // Use standalone BCrypt for password verification
        if ( !BCrypt.verifyer().verify( password.toCharArray(), user.getPassword() ).verified )
        {
            user.setFailedAttempts( user.getFailedAttempts() + 1 );
            if ( user.getFailedAttempts() >= 5 )
            {
                user.setLocked( true );
            }
            userRepository.save( user );
            return null;
        }

        if ( user.getFailedAttempts() > 0 )
        {
            user.setFailedAttempts( 0 );
            userRepository.save( user );
        }

        return user;
    }

    public UserSession createUserSession( User user )
    {
        List<String> permissions = new ArrayList<>();

        switch (user.getRole())
        {
            case ADMIN:
                permissions.add( "sites.manage" );
                permissions.add( "users.manage" );
                permissions.add( "files.manage" );
                permissions.add( "settings.manage" );
                permissions.add( "reports.view" );
                break;
            case USER:
                permissions.add( "sites.view" );
                permissions.add( "files.manage" );
                permissions.add( "reports.view" );
                break;
            case VIEWER:
                permissions.add( "sites.view" );
                permissions.add( "files.view" );
                break;
            default:
                permissions.add( "files.view" );
                break;
        }

        UserSession session = new UserSession( user.getUsername(), user.getRole(), permissions );
        session.setEmail( user.getEmail() );
        session.setFullName( user.getFullName() );
        return session;
    }

    @Transactional
    public void updateLastLogin( Long userId )
    {
        userRepository.updateLastLogin( userId, LocalDateTime.now() );
    }

    public User createUser( UserDTO userDTO )
    {
        if ( userRepository.existsByUsername( userDTO.getUsername() ) )
        {
            throw new RuntimeException( "Username already exists" );
        }

        User user = new User();
        user.setUsername( userDTO.getUsername() );
        // Use standalone BCrypt for password encoding
        user.setPassword( bcrypt.hashToString( 12, userDTO.getPassword().toCharArray() ) );
        user.setEmail( userDTO.getEmail() );
        user.setFullName( userDTO.getFullName() );
        user.setRole( UserRole.valueOf( userDTO.getRole() ) );
        user.setActive( true );
        user.setCreatedAt( LocalDateTime.now() );

        return userRepository.save( user );
    }

    public User updateUser( UserDTO userDTO )
    {
        User user = userRepository.findById( userDTO.getId() )
                .orElseThrow( () -> new RuntimeException( "User not found" ) );

        user.setEmail( userDTO.getEmail() );
        user.setFullName( userDTO.getFullName() );
        user.setRole( UserRole.valueOf( userDTO.getRole() ) );

        if ( userDTO.getPassword() != null && !userDTO.getPassword().isEmpty() )
        {
            // Use standalone BCrypt for password encoding
            user.setPassword( bcrypt.hashToString( 12, userDTO.getPassword().toCharArray() ) );
        }

        return userRepository.save( user );
    }

    public void deleteUser( Long userId )
    {
        if ( !userRepository.existsById( userId ) )
        {
            throw new RuntimeException( "User not found" );
        }
        userRepository.deleteById( userId );
    }

    public String resetUserPassword( Long userId )
    {
        User user = userRepository.findById( userId )
                .orElseThrow( () -> new RuntimeException( "User not found" ) );

        String tempPassword = UUID.randomUUID().toString().substring( 0, 8 );
        // Use standalone BCrypt for password encoding
        user.setPassword( bcrypt.hashToString( 12, tempPassword.toCharArray() ) );

        userRepository.save( user );
        return tempPassword;
    }

    public void toggleUserStatus( Long userId, boolean enable )
    {
        User user = userRepository.findById( userId )
                .orElseThrow( () -> new RuntimeException( "User not found" ) );

        user.setActive( enable );
        if ( enable )
        {
            user.setLocked( false );
            user.setFailedAttempts( 0 );
        }
        userRepository.save( user );
    }

    public void clearOldTransactionLogs( int daysOld )
    {
        log.info( "Clearing transaction logs older than {} days", daysOld );
    }

    public List<User> getAllUsers()
    {
        return userRepository.findAll();
    }

    public User getUserById( Long id )
    {
        return userRepository.findById( id )
                .orElseThrow( () -> new RuntimeException( "User not found" ) );
    }

    public long getUserCount()
    {
        return userRepository.count();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( UserService.class );
}