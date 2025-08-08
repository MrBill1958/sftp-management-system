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

import com.nearstar.sftpmanager.model.entity.ActivityLog;
import com.nearstar.sftpmanager.model.enums.ActivityType;

public class ActivityDTO
{
    private Long id;
    private String type;
    private String title;
    private String details;
    private String user;
    private String time;
    private String icon;
    private String color;

    // Default constructor
    public ActivityDTO()
    {
    }

    // Constructor from ActivityLog entity
    public ActivityDTO( ActivityLog log )
    {
        this.id = log.getId();
        this.type = log.getType().toString().toLowerCase();
        this.title = log.getTitle();
        this.details = log.getDetails();
        this.user = log.getUsername();
        this.time = log.getTimeAgo();
        setIconAndColor( log.getType() );
    }

    private void setIconAndColor( ActivityType type )
    {
        switch (type)
        {
            case UPLOAD:  // Now matches your enum
                this.icon = "⬆️";
                this.color = "upload";
                break;
            case DOWNLOAD:  // Now matches your enum
                this.icon = "⬇️";
                this.color = "download";
                break;
            case DELETE:  // Now matches your enum
                this.icon = "🗑️";
                this.color = "delete";
                break;
            case EDIT:  // Now matches your enum
                this.icon = "✏️";
                this.color = "edit";
                break;
            case LOGIN:
                this.icon = "🔑";
                this.color = "login";
                break;
            case LOGOUT:
                this.icon = "🚪";
                this.color = "logout";
                break;
            case CREATE:  // Now matches your enum
                this.icon = "➕";
                this.color = "create";
                break;
            case SFTP_CONNECTION_CREATED:
                this.icon = "🔌";
                this.color = "test";
                break;
            default:
                this.icon = "📄";
                this.color = "view";
        }
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

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle( String title )
    {
        this.title = title;
    }

    public String getDetails()
    {
        return details;
    }

    public void setDetails( String details )
    {
        this.details = details;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser( String user )
    {
        this.user = user;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime( String time )
    {
        this.time = time;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon( String icon )
    {
        this.icon = icon;
    }

    public String getColor()
    {
        return color;
    }

    public void setColor( String color )
    {
        this.color = color;
    }
}
