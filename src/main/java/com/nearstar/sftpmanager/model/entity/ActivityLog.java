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
package com.nearstar.sftpmanager.model.entity;

import com.nearstar.sftpmanager.model.enums.ActivityType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
public class ActivityLog
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ActivityType type;

    @Column(name = "title")
    private String title;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "site_name")
    private String siteName;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "ip_address")
    private String ipAddress;

    // Constructors
    public ActivityLog()
    {
        this.timestamp = LocalDateTime.now();
    }

    public ActivityLog( String username, ActivityType type, String title )
    {
        this();
        this.username = username;
        this.type = type;
        this.title = title;
    }

    public ActivityLog( String username, ActivityType type, String title, String details, String siteName )
    {
        this( username, type, title );
        this.details = details;
        this.siteName = siteName;
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

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public ActivityType getType()
    {
        return type;
    }

    public void setType( ActivityType type )
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

    public String getSiteName()
    {
        return siteName;
    }

    public void setSiteName( String siteName )
    {
        this.siteName = siteName;
    }

    public LocalDateTime getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp( LocalDateTime timestamp )
    {
        this.timestamp = timestamp;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress( String ipAddress )
    {
        this.ipAddress = ipAddress;
    }

    // Helper method to get time ago (for UI display)
    public String getTimeAgo()
    {
        if ( timestamp == null )
        {
            return "Unknown";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between( timestamp, now ).toMinutes();

        if ( minutes < 1 )
        {
            return "Just now";
        }
        if ( minutes == 1 )
        {
            return "1 minute ago";
        }
        if ( minutes < 60 )
        {
            return minutes + " minutes ago";
        }

        long hours = minutes / 60;
        if ( hours == 1 )
        {
            return "1 hour ago";
        }
        if ( hours < 24 )
        {
            return hours + " hours ago";
        }

        long days = hours / 24;
        if ( days == 1 )
        {
            return "1 day ago";
        }
        if ( days < 30 )
        {
            return days + " days ago";
        }

        long months = days / 30;
        if ( months == 1 )
        {
            return "1 month ago";
        }
        if ( months < 12 )
        {
            return months + " months ago";
        }

        long years = months / 12;
        return years + " year" + (years > 1 ? "s" : "") + " ago";
    }

    @Override
    public String toString()
    {
        return "ActivityLog{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", timestamp=" + timestamp +
                ", siteName='" + siteName + '\'' +
                '}';
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        ActivityLog that = (ActivityLog) o;
        return id != null && id.equals( that.id );
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }
}