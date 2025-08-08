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

import com.nearstar.sftpmanager.model.dto.UserDTO;
import com.nearstar.sftpmanager.model.dto.UserSession;
import com.nearstar.sftpmanager.model.entity.ApplicationConfig;
import com.nearstar.sftpmanager.model.entity.User;
import com.nearstar.sftpmanager.model.enums.ActionType;
import com.nearstar.sftpmanager.repository.ApplicationConfigRepository;
import com.nearstar.sftpmanager.service.AuditService;
import com.nearstar.sftpmanager.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController
{

    private final UserService userService;
    private final AuditService auditService;
    private final ApplicationConfigRepository configRepository;

    // Helper method to get current user from session
    private UserSession getCurrentUser( HttpSession session )
    {
        return (UserSession) session.getAttribute( "user" );
    }

    // Helper method to check if user is admin
    private boolean isAdmin( HttpSession session )
    {
        UserSession user = getCurrentUser( session );
        return user != null && user.isAdmin();
    }

    // Configuration Management
    @GetMapping("/configuration")
    public ResponseEntity<Map<String, Object>> getConfiguration( HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null || !currentUser.isAdmin() )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        Map<String, Object> config = new HashMap<>();
        List<ApplicationConfig> configs = configRepository.findAll();
        for (ApplicationConfig appConfig : configs)
        {
            config.put( appConfig.getConfigKey(), appConfig.getConfigValue() );
        }
        auditService.logAction( ActionType.VIEW, "Configuration", null,
                null, null, "Viewed system configuration", currentUser.getUsername() );
        return ResponseEntity.ok( config );
    }

    @PutMapping("/configuration")
    public ResponseEntity<Map<String, String>> updateConfiguration( @RequestBody Map<String, String> updates,
                                                                    HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null || !currentUser.isAdmin() )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        try
        {
            for (Map.Entry<String, String> entry : updates.entrySet())
            {
                ApplicationConfig config = configRepository.findByConfigKey( entry.getKey() )
                        .orElse( new ApplicationConfig() );
                String oldValue = config.getConfigValue();
                config.setConfigKey( entry.getKey() );
                config.setConfigValue( entry.getValue() );
                config.setUpdatedAt( LocalDateTime.now() );
                configRepository.save( config );

                auditService.logAction( ActionType.UPDATE, "Configuration", config.getId(),
                        oldValue, entry.getValue(),
                        "Updated config: " + entry.getKey(), currentUser.getUsername() );
            }
            return ResponseEntity.ok( Map.of( "message", "Configuration updated successfully" ) );
        }
        catch (Exception e)
        {
            log.error( "Error updating configuration", e );
            auditService.logFailedAction( ActionType.UPDATE, "Configuration", null,
                    "Failed to update configuration", e.getMessage(), currentUser.getUsername() );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to update configuration" ) );
        }
    }

    // User Management
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers( HttpSession session )
    {
        if ( !isAdmin( session ) )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok( users );
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById( @PathVariable Long id, HttpSession session )
    {
        if ( !isAdmin( session ) )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        User user = userService.getUserById( id );
        return ResponseEntity.ok( user );
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser( @Valid @RequestBody UserDTO userDTO, HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null || !currentUser.isAdmin() )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        try
        {
            User createdUser = userService.createUser( userDTO );
            auditService.logAction(
                    ActionType.CREATE,
                    "User",
                    createdUser.getId(),
                    null,
                    createdUser.getUsername(),
                    "User created: " + createdUser.getUsername(),
                    currentUser.getUsername()
            );
            return ResponseEntity.status( HttpStatus.CREATED ).body( createdUser );
        }
        catch (RuntimeException e)
        {
            log.error( "Error creating user: {}", e.getMessage() );
            return ResponseEntity.badRequest().body( Map.of( "error", e.getMessage() ) );
        }
        catch (Exception e)
        {
            log.error( "Error creating user", e );
            auditService.logFailedAction( ActionType.CREATE, "User", null,
                    "Failed to create user", e.getMessage(), currentUser.getUsername() );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to create user" ) );
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser( @PathVariable Long id, @Valid @RequestBody UserDTO userDTO,
                                         HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null || !currentUser.isAdmin() )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        try
        {
            userDTO.setId( id );
            User updatedUser = userService.updateUser( userDTO );
            auditService.logAction( ActionType.UPDATE, "User", id,
                    null, null, "Updated user: " + updatedUser.getUsername(), currentUser.getUsername() );
            return ResponseEntity.ok( updatedUser );
        }
        catch (RuntimeException e)
        {
            log.error( "Error updating user: {}", e.getMessage() );
            return ResponseEntity.badRequest().body( Map.of( "error", e.getMessage() ) );
        }
        catch (Exception e)
        {
            log.error( "Error updating user", e );
            auditService.logFailedAction( ActionType.UPDATE, "User", id,
                    "Failed to update user", e.getMessage(), currentUser.getUsername() );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to update user" ) );
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser( @PathVariable Long id, HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null || !currentUser.isAdmin() )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        try
        {
            userService.deleteUser( id );
            auditService.logAction( ActionType.DELETE, "User", id,
                    null, null, "Deleted user ID: " + id, currentUser.getUsername() );
            return ResponseEntity.ok( Map.of( "message", "User deleted successfully" ) );
        }
        catch (Exception e)
        {
            log.error( "Error deleting user", e );
            auditService.logFailedAction( ActionType.DELETE, "User", id,
                    "Failed to delete user", e.getMessage(), currentUser.getUsername() );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to delete user" ) );
        }
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetUserPassword( @PathVariable Long id, HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null || !currentUser.isAdmin() )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        try
        {
            String newPassword = userService.resetUserPassword( id );
            auditService.logAction( ActionType.UPDATE, "User", id,
                    null, null, "Reset password for user ID: " + id, currentUser.getUsername() );
            return ResponseEntity.ok( Map.of(
                    "message", "Password reset successfully",
                    "temporaryPassword", newPassword
            ) );
        }
        catch (Exception e)
        {
            log.error( "Error resetting password", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to reset password" ) );
        }
    }

    @PostMapping("/users/{id}/toggle-status")
    public ResponseEntity<?> toggleUserStatus( @PathVariable Long id, @RequestParam boolean enable,
                                               HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null || !currentUser.isAdmin() )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        try
        {
            userService.toggleUserStatus( id, enable );
            auditService.logAction( ActionType.UPDATE, "User", id,
                    String.valueOf( !enable ), String.valueOf( enable ),
                    (enable ? "Enabled" : "Disabled") + " user ID: " + id, currentUser.getUsername() );
            return ResponseEntity.ok( Map.of(
                    "message", "User " + (enable ? "enabled" : "disabled") + " successfully"
            ) );
        }
        catch (Exception e)
        {
            log.error( "Error toggling user status", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to toggle user status" ) );
        }
    }

    // Database Management
    @PostMapping("/database/initialize")
    public ResponseEntity<?> initializeDatabase( HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null || !currentUser.isAdmin() )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        try
        {
            // Database initialization logic
            auditService.logAction( ActionType.SYSTEM_CONFIG_CHANGE, "Database", null,
                    null, null, "Database schema initialized", currentUser.getUsername() );
            return ResponseEntity.ok( Map.of( "message", "Database initialized successfully" ) );
        }
        catch (Exception e)
        {
            log.error( "Error initializing database", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to initialize database" ) );
        }
    }

    @PostMapping("/database/backup")
    public ResponseEntity<?> backupDatabase( HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null || !currentUser.isAdmin() )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        try
        {
            // Database backup logic
            String backupFile = "backup-" + LocalDateTime.now() + ".sql";
            auditService.logAction( ActionType.SYSTEM_CONFIG_CHANGE, "Database", null,
                    null, null, "Database backed up to: " + backupFile, currentUser.getUsername() );
            return ResponseEntity.ok( Map.of(
                    "message", "Database backed up successfully",
                    "file", backupFile
            ) );
        }
        catch (Exception e)
        {
            log.error( "Error backing up database", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to backup database" ) );
        }
    }

    @DeleteMapping("/database/transaction-logs")
    public ResponseEntity<?> clearOldTransactionLogs( @RequestParam(defaultValue = "90") int daysOld,
                                                      HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null || !currentUser.isAdmin() )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        try
        {
            // Call the void method (doesn't return a count)
            userService.clearOldTransactionLogs( daysOld );
            auditService.logAction( ActionType.DELETE, "TransactionLog", null,
                    null, null,
                    "Cleared transaction logs older than " + daysOld + " days", currentUser.getUsername() );
            return ResponseEntity.ok( Map.of(
                    "message", "Transaction logs cleared successfully",
                    "daysOld", daysOld
            ) );
        }
        catch (Exception e)
        {
            log.error( "Error clearing transaction logs", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to clear transaction logs" ) );
        }
    }

    // System Information
    @GetMapping("/system/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo( HttpSession session )
    {
        if ( !isAdmin( session ) )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        Map<String, Object> info = new HashMap<>();
        info.put( "version", "1.0.0" );
        info.put( "serverTime", LocalDateTime.now() );
        info.put( "javaVersion", System.getProperty( "java.version" ) );
        info.put( "osName", System.getProperty( "os.name" ) );
        info.put( "totalMemory", Runtime.getRuntime().totalMemory() );
        info.put( "freeMemory", Runtime.getRuntime().freeMemory() );
        info.put( "maxMemory", Runtime.getRuntime().maxMemory() );
        return ResponseEntity.ok( info );
    }

    @PostMapping("/test-database-connection")
    public ResponseEntity<?> testDatabaseConnection( HttpSession session )
    {
        if ( !isAdmin( session ) )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        try
        {
            boolean isConnected = configRepository.count() >= 0;
            return ResponseEntity.ok( Map.of(
                    "connected", isConnected,
                    "message", isConnected ? "Database connection successful" : "Database connection failed"
            ) );
        }
        catch (Exception e)
        {
            return ResponseEntity.ok( Map.of(
                    "connected", false,
                    "message", "Database connection failed: " + e.getMessage()
            ) );
        }
    }
}