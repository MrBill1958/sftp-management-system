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

import com.nearstar.sftpmanager.model.enums.UserRole;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserSession implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String username;
    private String role;
    private List<String> permissions;
    private String email;
    private String fullName;
    private Long userId;

    // Constructors
    public UserSession()
    {
        this.permissions = new ArrayList<>();
    }

    public UserSession( String username, UserRole role, List<String> permissions )
    {
        this.username = username;
        this.role = role.getDisplayName();
        this.permissions = permissions != null ? permissions : new ArrayList<>();
    }

    // Helper method to check if user is admin
    public boolean isAdmin()
    {
        return "Administrator".equals( role ) || "ADMIN".equals( role );
    }

    // Helper method to check permissions
    public boolean hasPermission( String permission )
    {
        return permissions != null && permissions.contains( permission );
    }

    // Getters and Setters
    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole( String role )
    {
        this.role = role;
    }

    public List<String> getPermissions()
    {
        return permissions;
    }

    public void setPermissions( List<String> permissions )
    {
        this.permissions = permissions;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName( String fullName )
    {
        this.fullName = fullName;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId( Long userId )
    {
        this.userId = userId;
    }
}
