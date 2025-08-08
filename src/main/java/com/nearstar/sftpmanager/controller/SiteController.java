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

import com.nearstar.sftpmanager.model.dto.SiteDTO;
import com.nearstar.sftpmanager.model.dto.UserSession;
import com.nearstar.sftpmanager.model.entity.Site;
import com.nearstar.sftpmanager.service.SiteService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController
{

    private final SiteService siteService;

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

    @GetMapping
    public ResponseEntity<List<Site>> getAllSites( HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null )
        {
            return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).build();
        }

        if ( currentUser.isAdmin() )
        {
            // Admins can see all sites
            List<Site> sites = siteService.getAllSites();
            return ResponseEntity.ok( sites );
        }
        else
        {
            // Regular users can only see their sites
            List<Site> userSites = siteService.getUserSites( currentUser.getUsername() );
            return ResponseEntity.ok( userSites );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Site> getSiteById( @PathVariable Long id, HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null )
        {
            return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).build();
        }

        return siteService.getSiteById( id )
                .map( site -> ResponseEntity.ok( site ) )
                .orElse( ResponseEntity.notFound().build() );
    }

    @PostMapping
    public ResponseEntity<?> createSite( @Valid @RequestBody SiteDTO siteDTO, HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null )
        {
            return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).build();
        }

        // Only admins can create sites (adjust this logic as needed)
        if ( !currentUser.isAdmin() )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        try
        {
            // Pass the current username to the service
            Site createdSite = siteService.createSite( siteDTO, currentUser.getUsername() );
            return ResponseEntity.status( HttpStatus.CREATED ).body( createdSite );
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body( Map.of( "error", e.getMessage() ) );
        }
        catch (Exception e)
        {
            log.error( "Error creating site", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to create site" ) );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSite( @PathVariable Long id, @Valid @RequestBody SiteDTO siteDTO,
                                         HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null )
        {
            return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).build();
        }

        // Only admins can update sites (adjust this logic as needed)
        if ( !currentUser.isAdmin() )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        try
        {
            siteDTO.setId( id );
            // Pass the current username to the service
            Site updatedSite = siteService.updateSite( siteDTO, currentUser.getUsername() );
            return ResponseEntity.ok( updatedSite );
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body( Map.of( "error", e.getMessage() ) );
        }
        catch (Exception e)
        {
            log.error( "Error updating site", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to update site" ) );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSite( @PathVariable Long id, HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null )
        {
            return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).build();
        }

        // Only admins can delete sites (adjust this logic as needed)
        if ( !currentUser.isAdmin() )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        try
        {
            siteService.deleteSite( id );
            return ResponseEntity.ok( Map.of( "message", "Site deleted successfully" ) );
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body( Map.of( "error", e.getMessage() ) );
        }
        catch (Exception e)
        {
            log.error( "Error deleting site", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to delete site" ) );
        }
    }

    @PostMapping("/{id}/test-connection")
    public ResponseEntity<?> testConnection( @PathVariable Long id, HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null )
        {
            return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).build();
        }

        try
        {
            boolean isConnected = siteService.testConnection( id );
            return ResponseEntity.ok( Map.of(
                    "connected", isConnected,
                    "message", isConnected ? "Connection successful" : "Connection failed"
            ) );
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body( Map.of( "error", e.getMessage() ) );
        }
        catch (Exception e)
        {
            log.error( "Error testing connection", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to test connection" ) );
        }
    }

    @GetMapping("/user-sites")
    public ResponseEntity<List<Site>> getUserSites( HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null )
        {
            return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).build();
        }

        // Pass the current username to the service
        List<Site> userSites = siteService.getUserSites( currentUser.getUsername() );
        return ResponseEntity.ok( userSites );
    }

    @PostMapping("/{siteId}/assign-user/{userId}")
    public ResponseEntity<?> assignUserToSite( @PathVariable Long siteId, @PathVariable Long userId,
                                               HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null )
        {
            return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).build();
        }

        // Only admins can assign users to sites
        if ( !currentUser.isAdmin() )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        try
        {
            siteService.assignUserToSite( siteId, userId );
            return ResponseEntity.ok( Map.of( "message", "User assigned to site successfully" ) );
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body( Map.of( "error", e.getMessage() ) );
        }
        catch (Exception e)
        {
            log.error( "Error assigning user to site", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to assign user to site" ) );
        }
    }

    @DeleteMapping("/{siteId}/remove-user/{userId}")
    public ResponseEntity<?> removeUserFromSite( @PathVariable Long siteId, @PathVariable Long userId,
                                                 HttpSession session )
    {
        UserSession currentUser = getCurrentUser( session );
        if ( currentUser == null )
        {
            return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).build();
        }

        // Only admins can remove users from sites
        if ( !currentUser.isAdmin() )
        {
            return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
        }

        try
        {
            siteService.removeUserFromSite( siteId, userId );
            return ResponseEntity.ok( Map.of( "message", "User removed from site successfully" ) );
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body( Map.of( "error", e.getMessage() ) );
        }
        catch (Exception e)
        {
            log.error( "Error removing user from site", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to remove user from site" ) );
        }
    }
}