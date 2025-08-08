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
package com.nearstar.sftpmanager.model.dto;

import java.time.LocalDateTime;

public class FileDTO
{
    private String name;
    private String path;
    private boolean isDirectory;
    private long size;
    private String permissions;
    private LocalDateTime modified;
    private String owner;
    private String group;
    private String type;

    // Constructors
    public FileDTO()
    {
    }

    public FileDTO( String name, String path, boolean isDirectory, long size )
    {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
        this.size = size;
    }

    // Getters and Setters
    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    public boolean isDirectory()
    {
        return isDirectory;
    }

    public void setDirectory( boolean directory )
    {
        isDirectory = directory;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize( long size )
    {
        this.size = size;
    }

    public String getPermissions()
    {
        return permissions;
    }

    public void setPermissions( String permissions )
    {
        this.permissions = permissions;
    }

    public LocalDateTime getModified()
    {
        return modified;
    }

    public void setModified( LocalDateTime modified )
    {
        this.modified = modified;
    }

    public String getOwner()
    {
        return owner;
    }

    public void setOwner( String owner )
    {
        this.owner = owner;
    }

    public String getGroup()
    {
        return group;
    }

    public void setGroup( String group )
    {
        this.group = group;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    // Helper method to get human-readable file size
    public String getFormattedSize()
    {
        if ( size < 1024 )
        {
            return size + " B";
        }
        if ( size < 1024 * 1024 )
        {
            return String.format( "%.1f KB", size / 1024.0 );
        }
        if ( size < 1024 * 1024 * 1024 )
        {
            return String.format( "%.1f MB", size / (1024.0 * 1024.0) );
        }
        return String.format( "%.1f GB", size / (1024.0 * 1024.0 * 1024.0) );
    }
}
