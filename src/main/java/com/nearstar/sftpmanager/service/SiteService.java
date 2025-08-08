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

import com.nearstar.sftpmanager.model.dto.SiteDTO;
import com.nearstar.sftpmanager.model.entity.Site;
import com.nearstar.sftpmanager.model.entity.User;
import com.nearstar.sftpmanager.repository.SiteRepository;
import com.nearstar.sftpmanager.repository.UserRepository;
import com.nearstar.sftpmanager.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteService
{

    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final SftpService sftpService;

    @Transactional(readOnly = true)
    public List<Site> getAllSites()
    {
        return siteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Site> getSiteById( Long id )
    {
        return siteRepository.findById( id );
    }

    @Transactional
    public Site createSite( SiteDTO siteDTO, String currentUsername )
    {
        if ( siteRepository.existsBySiteName( siteDTO.getSiteName() ) )
        {
            throw new IllegalArgumentException( "Site name already exists" );
        }

        Site site = new Site();
        site.setSiteName( siteDTO.getSiteName() );
        site.setIpAddress( siteDTO.getIpAddress() );
        site.setPort( siteDTO.getPort() != null ? siteDTO.getPort() : 22 );
        site.setUsername( siteDTO.getUsername() );

        if ( siteDTO.getPassword() != null )
        {
            site.setEncryptedPassword( encryptionUtil.encrypt( siteDTO.getPassword() ) );
        }

        site.setTargetPath( siteDTO.getTargetPath() != null ? siteDTO.getTargetPath() : "/" );
        site.setDescription( siteDTO.getDescription() );
        site.setEmailNotification( siteDTO.getEmailNotification() );
        site.setSshKey( siteDTO.getSshKey() );
        site.setKnownHostsEntry( siteDTO.getKnownHostsEntry() );

        // Set creator/owner using provided username instead of Spring Security
        if ( currentUsername != null )
        {
            userRepository.findByUsername( currentUsername ).ifPresent( user ->
            {
                site.setCreatedBy( user );
                site.setOwner( user );
            } );
        }

        site.setCreatedAt( LocalDateTime.now() );

        return siteRepository.save( site );
    }

    @Transactional
    public Site updateSite( SiteDTO siteDTO, String currentUsername )
    {
        Site site = siteRepository.findById( siteDTO.getId() )
                .orElseThrow( () -> new IllegalArgumentException( "Site not found" ) );

        site.setSiteName( siteDTO.getSiteName() );
        site.setIpAddress( siteDTO.getIpAddress() );
        site.setPort( siteDTO.getPort() != null ? siteDTO.getPort() : 22 );
        site.setUsername( siteDTO.getUsername() );

        if ( siteDTO.getPassword() != null && !siteDTO.getPassword().isEmpty() )
        {
            site.setEncryptedPassword( encryptionUtil.encrypt( siteDTO.getPassword() ) );
        }

        site.setTargetPath( siteDTO.getTargetPath() );
        site.setDescription( siteDTO.getDescription() );
        site.setEmailNotification( siteDTO.getEmailNotification() );
        site.setSshKey( siteDTO.getSshKey() );
        site.setKnownHostsEntry( siteDTO.getKnownHostsEntry() );

        // Set updater using provided username instead of Spring Security
        if ( currentUsername != null )
        {
            userRepository.findByUsername( currentUsername ).ifPresent( user ->
            {
                site.setUpdatedBy( user );
            } );
        }

        site.setUpdatedAt( LocalDateTime.now() );

        return siteRepository.save( site );
    }

    @Transactional
    public void deleteSite( Long id )
    {
        if ( !siteRepository.existsById( id ) )
        {
            throw new IllegalArgumentException( "Site not found" );
        }
        siteRepository.deleteById( id );
    }

    @Transactional
    public boolean testConnection( Long siteId )
    {
        Site site = siteRepository.findById( siteId )
                .orElseThrow( () -> new IllegalArgumentException( "Site not found" ) );

        try
        {
            SftpService.TestConnectionResult result = sftpService.testConnection( site );
            return result.isSuccess();
        }
        catch (Exception e)
        {
            log.error( "Error testing connection for site {}: {}", siteId, e.getMessage() );
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<Site> getUserSites( String username )
    {
        if ( username != null )
        {
            return siteRepository.findUserSites( username );
        }
        return List.of();
    }

    @Transactional
    public void assignUserToSite( Long siteId, Long userId )
    {
        Site site = siteRepository.findById( siteId )
                .orElseThrow( () -> new IllegalArgumentException( "Site not found" ) );

        User user = userRepository.findById( userId )
                .orElseThrow( () -> new IllegalArgumentException( "User not found" ) );

        // TODO: Implement user-site assignment if needed
        siteRepository.save( site );
    }

    @Transactional
    public void removeUserFromSite( Long siteId, Long userId )
    {
        Site site = siteRepository.findById( siteId )
                .orElseThrow( () -> new IllegalArgumentException( "Site not found" ) );

        User user = userRepository.findById( userId )
                .orElseThrow( () -> new IllegalArgumentException( "User not found" ) );

        // TODO: Implement user-site removal if needed
        siteRepository.save( site );
    }
}