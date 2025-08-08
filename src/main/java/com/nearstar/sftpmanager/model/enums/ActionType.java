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
package com.nearstar.sftpmanager.model.enums;

/**
 * Enum representing audit action types (used in audit_logs table)
 */
public enum ActionType
{
    CREATE( "Create", "Created a new resource" ),
    UPDATE( "Update", "Updated an existing resource" ),
    DELETE( "Delete", "Deleted a resource" ),
    LOGIN( "Login", "User login" ),
    LOGOUT( "Logout", "User logout" ),
    VIEW( "View", "Viewed a resource" ),
    TEST_CONNECTION( "Test Connection", "Tested SFTP connection" ),
    UPLOAD_FILE( "Upload File", "Uploaded a file" ),
    DOWNLOAD_FILE( "Download File", "Downloaded a file" ),
    DELETE_FILE( "Delete File", "Deleted a file" ),
    CREATE_FOLDER( "Create Folder", "Created a folder" ),
    CHANGE_PASSWORD( "Change Password", "Changed password" ),
    GRANT_ACCESS( "Grant Access", "Granted access to user" ),
    REVOKE_ACCESS( "Revoke Access", "Revoked access from user" ),
    EXPORT_AUDIT( "Export Audit", "Exported audit logs" ),
    SYSTEM_CONFIG_CHANGE( "System Config Change", "Changed system configuration" );

    private final String displayName;
    private final String description;

    ActionType( String displayName, String description )
    {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getDescription()
    {
        return description;
    }

    /**
     * Check if this action type requires admin privileges
     * @return true if admin privileges are required
     */
    public boolean requiresAdmin()
    {
        return this == GRANT_ACCESS ||
                this == REVOKE_ACCESS ||
                this == SYSTEM_CONFIG_CHANGE ||
                this == EXPORT_AUDIT;
    }

    /**
     * Get ActionType from string value
     * @param value the string value
     * @return the corresponding ActionType or VIEW if not found
     */
    public static ActionType fromString( String value )
    {
        if ( value == null )
        {
            return VIEW;
        }

        // Handle underscore and hyphen variations
        value = value.toUpperCase().replace( "-", "_" );

        try
        {
            return ActionType.valueOf( value );
        }
        catch (IllegalArgumentException e)
        {
            return VIEW;
        }
    }
}