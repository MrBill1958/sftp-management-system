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

public class DashboardStats
{
    private int totalSites;
    private int activeSites;
    private int totalUsers;
    private int recentFiles;
    private long totalStorage;
    private int onlineUsers;
    private int totalActivities;
    private String lastBackup;

    // Constructor
    public DashboardStats()
    {
        // Initialize with default values
        this.totalSites = 0;
        this.activeSites = 0;
        this.totalUsers = 0;
        this.recentFiles = 0;
        this.totalStorage = 0L;
        this.onlineUsers = 0;
        this.totalActivities = 0;
    }

    // Getters and Setters
    public int getTotalSites()
    {
        return totalSites;
    }

    public void setTotalSites( int totalSites )
    {
        this.totalSites = totalSites;
    }

    public int getActiveSites()
    {
        return activeSites;
    }

    public void setActiveSites( int activeSites )
    {
        this.activeSites = activeSites;
    }

    public int getTotalUsers()
    {
        return totalUsers;
    }

    public void setTotalUsers( int totalUsers )
    {
        this.totalUsers = totalUsers;
    }

    public int getRecentFiles()
    {
        return recentFiles;
    }

    public void setRecentFiles( int recentFiles )
    {
        this.recentFiles = recentFiles;
    }

    public long getTotalStorage()
    {
        return totalStorage;
    }

    public void setTotalStorage( long totalStorage )
    {
        this.totalStorage = totalStorage;
    }

    public int getOnlineUsers()
    {
        return onlineUsers;
    }

    public void setOnlineUsers( int onlineUsers )
    {
        this.onlineUsers = onlineUsers;
    }

    public int getTotalActivities()
    {
        return totalActivities;
    }

    public void setTotalActivities( int totalActivities )
    {
        this.totalActivities = totalActivities;
    }

    public String getLastBackup()
    {
        return lastBackup;
    }

    public void setLastBackup( String lastBackup )
    {
        this.lastBackup = lastBackup;
    }

    // Helper method to format storage size
    public String getFormattedStorage()
    {
        if ( totalStorage < 1024 )
        {
            return totalStorage + " B";
        }
        if ( totalStorage < 1048576 )
        {
            return (totalStorage / 1024) + " KB";
        }
        if ( totalStorage < 1073741824 )
        {
            return (totalStorage / 1048576) + " MB";
        }
        return String.format( "%.2f GB", totalStorage / 1073741824.0 );
    }
}
