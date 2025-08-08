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

import com.nearstar.sftpmanager.model.enums.UserRole;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User
{  // Removed UserDetails implementation

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    // Optional additional name fields (if used)
    @Column(name = "firstName")
    private String firstName;

    @Column(name = "lastName")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    // Map to database column names
    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "is_locked")
    private boolean locked = false;

    @Column(name = "failed_attempts")
    private int failedAttempts = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // Additional fields that exist in database
    @Column(name = "accountNonExpired")
    private boolean accountNonExpired = true;

    @Column(name = "accountNonLocked")
    private boolean accountNonLocked = true;

    @Column(name = "credentialsNonExpired")
    private boolean credentialsNonExpired = true;

    @Column(name = "enabled")
    private boolean enabled = true;

    @Column(name = "mustChangePassword")
    private boolean mustChangePassword = false;

    @Column(name = "passwordChangedAt")
    private LocalDateTime passwordChangedAt;

    @Column(name = "lastLogin") // Additional lastLogin field
    private LocalDateTime lastLoginDateTime;

    @Column(name = "access_group_id")
    private Long accessGroupId;

    // Constructors
    public User()
    {
        this.createdAt = LocalDateTime.now();
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

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
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

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName( String firstName )
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName( String lastName )
    {
        this.lastName = lastName;
    }

    public UserRole getRole()
    {
        return role;
    }

    public void setRole( UserRole role )
    {
        this.role = role;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive( boolean active )
    {
        this.active = active;
    }

    public boolean isLocked()
    {
        return locked;
    }

    public void setLocked( boolean locked )
    {
        this.locked = locked;
    }

    public int getFailedAttempts()
    {
        return failedAttempts;
    }

    public void setFailedAttempts( int failedAttempts )
    {
        this.failedAttempts = failedAttempts;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt( LocalDateTime createdAt )
    {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin()
    {
        return lastLogin;
    }

    public void setLastLogin( LocalDateTime lastLogin )
    {
        this.lastLogin = lastLogin;
    }

    // Additional getters/setters for database fields
    public boolean getAccountNonExpired()
    {
        return accountNonExpired;
    }

    public void setAccountNonExpired( boolean accountNonExpired )
    {
        this.accountNonExpired = accountNonExpired;
    }

    public boolean getAccountNonLocked()
    {
        return accountNonLocked;
    }

    public void setAccountNonLocked( boolean accountNonLocked )
    {
        this.accountNonLocked = accountNonLocked;
    }

    public boolean getCredentialsNonExpired()
    {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired( boolean credentialsNonExpired )
    {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
    }

    public boolean isMustChangePassword()
    {
        return mustChangePassword;
    }

    public void setMustChangePassword( boolean mustChangePassword )
    {
        this.mustChangePassword = mustChangePassword;
    }

    public LocalDateTime getPasswordChangedAt()
    {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt( LocalDateTime passwordChangedAt )
    {
        this.passwordChangedAt = passwordChangedAt;
    }

    public LocalDateTime getLastLoginDateTime()
    {
        return lastLoginDateTime;
    }

    public void setLastLoginDateTime( LocalDateTime lastLoginDateTime )
    {
        this.lastLoginDateTime = lastLoginDateTime;
    }

    public Long getAccessGroupId()
    {
        return accessGroupId;
    }

    public void setAccessGroupId( Long accessGroupId )
    {
        this.accessGroupId = accessGroupId;
    }
}