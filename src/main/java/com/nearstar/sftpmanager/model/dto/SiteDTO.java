/**
 * NearStar, Inc.
 * 410 E. Main Street
 * Lewisville, Texas  76057
 * Tel: 1.972.221.4068
 * <p>
 * Copyright © 2025 NearStar Incorporated. All rights reserved.
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
package com.nearstar.sftpmanager.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nearstar.sftpmanager.model.entity.Site;

public class SiteDTO
{
    private Long id;
    private String siteName;
    private String ipAddress;
    private Integer port;
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String targetPath;
    private String description;
    private String emailNotification;
    private String sshKey;
    private String knownHostsEntry;
    private boolean active;
    private String connectionStatus;
    private String lastTested;
    private boolean hasPassword;
    private int passwordLength;

    // Default constructor
    public SiteDTO()
    {
    }

    // Constructor from Site entity
    public SiteDTO( Site site )
    {
        this.id = site.getId();
        this.siteName = site.getSiteName();
        this.ipAddress = site.getIpAddress();
        this.port = site.getPort();
        this.username = site.getUsername();
        this.targetPath = site.getTargetPath();
        this.description = site.getDescription();
        this.emailNotification = site.getEmailNotification();
        this.sshKey = site.getSshKey();
        this.knownHostsEntry = site.getKnownHostsEntry();

        // REMOVED: this.active = site.isActive(); - method doesn't exist
        this.active = true; // Default value

        this.connectionStatus = site.getConnectionStatus() != null ? site.getConnectionStatus().toString() : null;
        this.lastTested = site.getLastTested() != null ? site.getLastTested().toString() : null;
        this.hasPassword = site.getEncryptedPassword() != null && !site.getEncryptedPassword().isEmpty();
        this.passwordLength = site.getEncryptedPassword() != null ? site.getEncryptedPassword().length() : 0;
        // Don't expose actual password in DTO
        this.password = this.hasPassword ? "••••••••" : null;
    }

    // Convert DTO back to Site entity
    public Site toEntity()
    {
        Site site = new Site();
        site.setId( this.id );
        site.setSiteName( this.siteName );
        site.setIpAddress( this.ipAddress );
        site.setPort( this.port );
        site.setUsername( this.username );
        site.setTargetPath( this.targetPath );
        site.setDescription( this.description );
        site.setEmailNotification( this.emailNotification );
        site.setSshKey( this.sshKey );
        site.setKnownHostsEntry( this.knownHostsEntry );

        // REMOVED: site.setActive(this.active); - method doesn't exist

        // Password should be handled separately for security
        return site;
    }

    // Getters and Setters
    public Long getId()
    {
        return id;
    }

    public void setId( Long id )
    {
        this.id = id;
    }

    public String getSiteName()
    {
        return siteName;
    }

    public void setSiteName( String siteName )
    {
        this.siteName = siteName;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress( String ipAddress )
    {
        this.ipAddress = ipAddress;
    }

    public Integer getPort()
    {
        return port;
    }

    public void setPort( Integer port )
    {
        this.port = port;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public String getTargetPath()
    {
        return targetPath;
    }

    public void setTargetPath( String targetPath )
    {
        this.targetPath = targetPath;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getEmailNotification()
    {
        return emailNotification;
    }

    public void setEmailNotification( String emailNotification )
    {
        this.emailNotification = emailNotification;
    }

    public String getSshKey()
    {
        return sshKey;
    }

    public void setSshKey( String sshKey )
    {
        this.sshKey = sshKey;
    }

    public String getKnownHostsEntry()
    {
        return knownHostsEntry;
    }

    public void setKnownHostsEntry( String knownHostsEntry )
    {
        this.knownHostsEntry = knownHostsEntry;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive( boolean active )
    {
        this.active = active;
    }

    public String getConnectionStatus()
    {
        return connectionStatus;
    }

    public void setConnectionStatus( String connectionStatus )
    {
        this.connectionStatus = connectionStatus;
    }

    public String getLastTested()
    {
        return lastTested;
    }

    public void setLastTested( String lastTested )
    {
        this.lastTested = lastTested;
    }

    public boolean isHasPassword()
    {
        return hasPassword;
    }

    public void setHasPassword( boolean hasPassword )
    {
        this.hasPassword = hasPassword;
    }

    public int getPasswordLength()
    {
        return passwordLength;
    }

    public void setPasswordLength( int passwordLength )
    {
        this.passwordLength = passwordLength;
    }
}
